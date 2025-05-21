package Database;

import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionJDBC {
    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("Driver loaded");

            String url = "jdbc:sqlserver://localhost:49828;databaseName=DoAnCoSo1;encrypt=true;trustServerCertificate=true;loginTimeout=30;socketTimeout=60";
            String user = "superadmin";
            String password = "admin123";

            con = DriverManager.getConnection(url,user,password);
            System.out.println("Connection established");

        }catch (ClassNotFoundException e) {
            System.out.println("SQL server JDBC Driver Not Found!" + e.getMessage());
            e.printStackTrace();
        }catch (SQLException e) {
            System.out.println("Connection failed!" + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Database connection failed: " + e.getMessage());

            alert.showAndWait();
            return null;
        }
        return con;
    }
}
