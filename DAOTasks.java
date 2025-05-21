package DAO;

import Database.ConnectionJDBC;
import LinkTasksAndCal.LinkTasksAndCalModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DAOTasks implements DAOInterfaceTasks<LinkTasksAndCalModel> {

    public static DAOTasks getInstance(){
        return new DAOTasks();
    }

    @Override
    public int delete(LinkTasksAndCalModel obj) {
        try (Connection con = ConnectionJDBC.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Table_Tasks WHERE ID = ?")){
            System.out.println("Đang xóa nhiệm vụ với ID: " + obj.getId());
            ps.setInt(1, obj.getId());

            int rowsAffected = ps.executeUpdate();
            System.out.println(rowsAffected + " row(s) deleted");
            return rowsAffected;

        }catch (SQLException e) {
            throw new RuntimeException("Error while deleting task" + e.getMessage() + e);
        }
    }

    @Override
    public int update(LinkTasksAndCalModel obj) {
        // Chuyển hướng sang saveTaskToDatabase với isNew = false
        saveTaskToDatabase(obj, false);
        return 1; // Giả định cập nhật thành công nếu không có ngoại lệ
    }

    @Override
    public LinkTasksAndCalModel loadTaskById(Integer id) {
        if (id == null || id <= 0) {
            System.err.println("Invalid task ID: " + id);
            return null;
        }
        LinkTasksAndCalModel task = null;
        String sql = "SELECT * FROM  Table_Tasks WHERE ID = ?";
        try (Connection conn = ConnectionJDBC.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id); // Đảm bảo id không null
            System.out.println("Executing query: " + sql + " with ID: " + id); // Log để debug
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                task = new LinkTasksAndCalModel();
                task.setId(rs.getInt("ID"));
                task.setTitle(rs.getString("Title"));
                task.setDescription(rs.getString("Description"));
                task.setStartTime(rs.getTimestamp("StartTime") != null ? rs.getTimestamp("StartTime").toLocalDateTime() : null);
                task.setEndTime(rs.getTimestamp("EndTime") != null ? rs.getTimestamp("EndTime").toLocalDateTime() : null);
                task.setCompleted(rs.getBoolean("Completed"));
                task.setDuration(rs.getLong("Duration"));
                task.setTimeSpentHours(rs.getInt("TimeSpentHours"));
                task.setTimeSpentMinutes(rs.getInt("TimeSpentMinutes"));
                task.setRecurrence(rs.getString("Recurrence"));
                task.setPriority(rs.getString("Priority"));
            } else {
                System.out.println("No task found with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return task;
    }

    @Override
    public ObservableList<LinkTasksAndCalModel> selectAllTasks() {
            ObservableList<LinkTasksAndCalModel> tasksList = FXCollections.observableArrayList();

            try (Connection connection = ConnectionJDBC.getConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT * FROM Table_Tasks");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinkTasksAndCalModel task = new LinkTasksAndCalModel(
                            rs.getInt("ID"),
                            rs.getString("Title"),
                            rs.getString("Description"),
                            rs.getTimestamp("StartTime") != null ? rs.getTimestamp("StartTime").toLocalDateTime() : null,
                            rs.getTimestamp("EndTime") != null ? rs.getTimestamp("EndTime").toLocalDateTime() : null,
                            rs.getBoolean("Completed"),
                            rs.getObject("Duration") != null ? rs.getInt("Duration") : 0,
                            rs.getObject("TimeSpentHours") != null ? rs.getInt("TimeSpentHours") : 0,
                            rs.getObject("TimeSpentMinutes") != null ? rs.getInt("TimeSpentMinutes") : 0,
                            rs.getString("Recurrence"),
                            rs.getString("Priority")
                    );
                    tasksList.add(task);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error while selecting all tasks: " + e.getMessage(), e);
            }
            return tasksList;
        }

    @Override
    public void saveTaskToDatabase(LinkTasksAndCalModel task, boolean isNew) {
        try (Connection connection = ConnectionJDBC.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     isNew ? "INSERT INTO Table_Tasks (Title, Description, StartTime, EndTime, Completed, Duration, TimeSpentHours, TimeSpentMinutes, Recurrence, Priority) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" :
                             "UPDATE Table_Tasks SET Title = ?, Description = ?, StartTime = ?, EndTime = ?, Completed = ?, Duration = ?, TimeSpentHours = ?, TimeSpentMinutes = ?, Recurrence = ?, Priority = ? WHERE Id = ?",
                     Statement.RETURN_GENERATED_KEYS)) {
            if (isNew) {

                // Kiểm tra tiêu đề hợp lệ
                if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                    throw new IllegalArgumentException("Tiêu đề nhiệm vụ không được để trống.");
                }
                // Kiểm tra tiêu đề trùng lặp
                if (doesTitleExist(task.getTitle())) {
                    throw new SQLException("Nhiệm vụ với tiêu đề '" + task.getTitle() + "' đã tồn tại.");
                }

                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setTimestamp(3, task.getStartTime() != null ? Timestamp.valueOf(task.getStartTime()) : null);
                pstmt.setTimestamp(4, task.getEndTime() != null ? Timestamp.valueOf(task.getEndTime()) : null);
                pstmt.setBoolean(5, task.isCompleted());
                pstmt.setLong(6, task.getDuration());
                pstmt.setInt(7, task.getTimeSpentHours());
                pstmt.setInt(8, task.getTimeSpentMinutes());
                pstmt.setString(9, task.getRecurrence());
                pstmt.setString(10, task.getPriority());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println(rowsAffected + " row(s) inserted");
                } else {
                    System.out.println("No rows inserted");
                }

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                }
            } else {
                LinkTasksAndCalModel currentTask = loadTaskById(task.getId());

                if (currentTask == null) {
                    throw new RuntimeException("Task with ID " + task.getId() + " not found");
                }

                // Kiểm tra tiêu đề trùng lặp (trừ bản ghi hiện tại)
                if (task.getTitle() != null && !task.getTitle().trim().equals(currentTask.getTitle()) && doesTitleExist(task.getTitle())) {
                    throw new SQLException("Nhiệm vụ với tiêu đề '" + task.getTitle() + "' đã tồn tại.");
                }

                pstmt.setString(1, task.getTitle() != null ? task.getTitle().trim() : currentTask.getTitle());
                pstmt.setString(2, task.getDescription() != null ? task.getDescription() : currentTask.getDescription());
                pstmt.setTimestamp(3, task.getStartTime() != null ? Timestamp.valueOf(task.getStartTime()) :
                        (currentTask.getStartTime() != null ? Timestamp.valueOf(currentTask.getStartTime()) : null));
                pstmt.setTimestamp(4, task.getEndTime() != null ? Timestamp.valueOf(task.getEndTime()) :
                        (currentTask.getEndTime() != null ? Timestamp.valueOf(currentTask.getEndTime()) : null));
                pstmt.setBoolean(5, task.isCompleted());
                // Chỉ cập nhật nếu giá trị mới được cung cấp (khác với mặc định)
                pstmt.setLong(6, task.getDuration() != Long.MIN_VALUE ? task.getDuration() : currentTask.getDuration());
                pstmt.setInt(7, task.getTimeSpentHours() != Integer.MIN_VALUE ? task.getTimeSpentHours() : currentTask.getTimeSpentHours());
                pstmt.setInt(8, task.getTimeSpentMinutes() != Integer.MIN_VALUE ? task.getTimeSpentMinutes() : currentTask.getTimeSpentMinutes());
                pstmt.setString(9, task.getRecurrence() != null ? task.getRecurrence() : currentTask.getRecurrence());
                pstmt.setString(10, task.getPriority() != null ? task.getPriority() : currentTask.getPriority());
                pstmt.setInt(11, task.getId());

                int rowsAffected = pstmt.executeUpdate();
                System.out.println(rowsAffected + " row(s) updated");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu nhiệm vụ: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean doesTitleExist(String title){
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        try (Connection connection = ConnectionJDBC.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM Table_Tasks WHERE Title = ?")) {
            stmt.setString(1, title.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}


