package DAO;

import Database.ConnectionJDBC;
import Model.UserModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAOUser implements DAOInterfaceUser<UserModel> {
    public static DAOUser getInstance(){
        return new DAOUser();
    }
    @Override
    public int insert(UserModel obj) {
            try (Connection con = ConnectionJDBC.getConnection();
                 PreparedStatement pre = con.prepareStatement("INSERT INTO Table_User (Username, Password, ConfirmPassword, Gender, DOB) VALUES (?, ?, ?, ?, ?)")){
                pre.setString(1,obj.getUsername());
                pre.setString(2, obj.getPassword());
                pre.setString(3, obj.getConfirmPassword());
                pre.setString(4, obj.getGender());
                // Chuyển LocalDate thành java.sql.Date
                if (obj.getDOB() != null) {
                    pre.setDate(5, java.sql.Date.valueOf(obj.getDOB()));
                } else {
                    pre.setNull(5, java.sql.Types.DATE); // Xử lý trường hợp DOB là null
                }

                int rowsAffected = pre.executeUpdate();
                System.out.println(rowsAffected + " row(s) inserted");
                return rowsAffected;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }

    @Override
    public boolean isUsernameExist(String username) {
        try(Connection con = ConnectionJDBC.getConnection();
        PreparedStatement pre = con.prepareStatement("SELECT COUNT(*) FROM Table_User WHERE username = ?")){
            pre.setString(1,username);
            ResultSet rs = pre.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Trả về true nếu username đã tồn tại
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
