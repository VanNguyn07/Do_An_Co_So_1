package Model;

import java.time.LocalDate;

public class UserModel {
    private String username;
    private String password;
    private String confirmPassword;
    private String gender;
    private LocalDate DOB;

    public UserModel(String username, String password,String confirmPassword, String gender, LocalDate DOB) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.gender = gender;
        this.DOB = DOB;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDOB() {
        return DOB;
    }

    public void setDOB(LocalDate DOB) {
        this.DOB = DOB;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
