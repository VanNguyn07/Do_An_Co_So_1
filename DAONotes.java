package DAO;

import Database.ConnectionJDBC;
import Model.NotesModel;

import java.sql.*;
import java.util.ArrayList;

public class DAONotes implements DAOInterfaceNotes<NotesModel> {

    public static DAONotes getInstance() {
        return new DAONotes();
    }

    @Override
    public int delete(NotesModel obj) {
        try {
            Connection connection = ConnectionJDBC.getConnection();
            String sql = "DELETE FROM Table_NotesDelete " + "WHERE Name = ?";
            PreparedStatement prepared = connection.prepareStatement(sql);

            prepared.setString(1, obj.getName());

            int check = prepared.executeUpdate();
            System.out.println(check + " rows deleted");

            connection.close();
            prepared.close();
            return check;
        }catch(SQLException e) {
            throw new RuntimeException("Không thể xóa ghi chú: " + e.getMessage(), e);
        }
    }

    @Override
    public int insert(NotesModel obj) {
        try {
            Connection connection = ConnectionJDBC.getConnection();
            String sql = "INSERT INTO Table_NotesDelete " + "(Name, Content) " + "VALUES (?, ?)";

            PreparedStatement pre = connection.prepareStatement(sql);
            pre.setString(1, obj.getName());
            pre.setString(2, obj.getContent());

            pre.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm ghi chú: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int update(NotesModel obj) {
        try {
            Connection connection = ConnectionJDBC.getConnection();
            String sql = "UPDATE Table_NotesDelete " + "SET Content = ? " + "WHERE Name = ?";

            PreparedStatement pre = connection.prepareStatement(sql);

            pre.setString(1, obj.getContent());
            pre.setString(2, obj.getName());

            pre.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật ghi chú: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public ArrayList<NotesModel> selectAll() {
        ArrayList<NotesModel> notesModels = new ArrayList<>();
        try {
            Connection connection = ConnectionJDBC.getConnection();

            Statement statement = connection.createStatement();

            String sql = "SELECT * FROM Table_NotesDelete";

            ResultSet resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
                notesModels.add(new NotesModel(resultSet.getString("Name"), resultSet.getString("Content")));
            }
        }catch (SQLException e) {
            throw new RuntimeException("Không thể lấy danh sách ghi chú: " + e.getMessage(), e);
        }
        return notesModels;
    }

}
