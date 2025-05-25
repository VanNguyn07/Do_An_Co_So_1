package Model;

import Database.ConnectionJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AccountManagement {

    public ArrayList<String> getInstance(String inputUsername, String inputPassword, String inputGender) {
        // trả về một ArrayList với 2 tham số để check thông tin đăng nhập

        ArrayList<String> getDataFromCSDL = new ArrayList<>();
        // luôn luôn khởi tạo danh sách để lưu thông tin dữ liệu từ CSDL
        try {
            Connection conn = ConnectionJDBC.getConnection();

            String sql = "SELECT * FROM Table_User " +
                    " WHERE Username = ? AND Password = ? AND Gender = ?";
            // Tạo một mẫu câu lệnh SQL cố định và chx gắn kết CSDL với các tham số là placeholder (?)

            PreparedStatement pre = conn.prepareStatement(sql);// câu lệnh SQL được gửi đến CSDL để chuẩn bị trước

            pre.setString(1, inputUsername);
            pre.setString(2, inputPassword);
            pre.setString(3, inputGender);
            // gán giá trị thực tế vào hai dấu ? in câu lệnh SQL

            // Thực thi câu lệnh
            ResultSet rs = pre.executeQuery();

            while (rs.next()) {
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                String gender  = rs.getString("Gender");

                getDataFromCSDL.add(username);
                getDataFromCSDL.add(password);
                getDataFromCSDL.add(gender);
            }
            System.out.println("Input Username: " + inputUsername);
            System.out.println("Input Password: " + inputPassword);
            System.out.println("Input Gender: " + inputGender);
            conn.close();
            pre.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getDataFromCSDL;
    }

    public String getDataFromCSDL(String inputUsername){
        String username = null;
        try(Connection con = ConnectionJDBC.getConnection();
        PreparedStatement pre = con.prepareStatement("SELECT Username FROM Table_User WHERE Username = ?")){
            pre.setString(1, inputUsername);
            ResultSet rs = pre.executeQuery();
            if(rs.next()){
                username = rs.getString("Username");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }
}
