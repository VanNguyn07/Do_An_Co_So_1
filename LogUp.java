package LogUp;

import DAO.DAOUser;
import LogIn.LogIn;
import Model.UserModel;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LogUp extends Application {
    private Label labelName, labelPassword, labelConfirmPassword,labelGender, labelDOB;
    private TextField txtName;
    private PasswordField txtConfirmPassword, txtPassword;
    private ComboBox<String> cmbGender;
    private Button btnLogUp;
    private VBox vBoxName = new VBox();
    private VBox vBoxPassword = new VBox();
    private VBox vBoxConfirmPassword = new VBox();
    private VBox vBoxNameAndPass = new VBox(10);
    private VBox vBoxComboBox = new VBox();
    private VBox vBoxDOB = new VBox(10);
    private DatePicker datePicker;
    private Stage currentStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.currentStage = stage;// Lưu Stage hiện tại

        // Tạo HBox làm container chính để điều chỉnh vị trí ngang
        HBox rootContainer = new HBox(); // Khoảng cách giữa các thành phần (nếu có)
        rootContainer.setAlignment(Pos.CENTER); // Căn giữa theo chiều dọc
        rootContainer.setPadding(new Insets(20, 20, 20, 30)); // Thêm padding trái 50px để đẩy sang phải
        rootContainer.setBackground(new Background(new BackgroundFill(Color.SEASHELL, new CornerRadii(20), new Insets(5))));

        // Tạo VBox cho nội dung chính
        VBox root = new VBox(10);
        root.setPrefSize(400,550);
        root.setAlignment(Pos.CENTER);

        Label labelLogUp = new Label("Log Up");
        labelLogUp.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 40));
        root.getChildren().add(labelLogUp);

        initLabelAndTextField(root);

        // Thêm root vào HBox
        rootContainer.getChildren().add(root);

        Scene scene = new Scene(rootContainer);
        stage.setTitle("Log Up");
        stage.setScene(scene);
        initIconLogUp(stage);
        stage.show();
    }

    private void initIconLogUp(Stage stage) {
        Image imageLogUp = new Image(getClass().getResourceAsStream("/IconLogUp.png"));
        stage.getIcons().add(imageLogUp);
    }

    private void initLabelAndTextField(VBox root) {

        labelName = createCustomLabel("User Name:");
        txtName = createCustomTextField("Enter Full Name!");
        vBoxName = createCustomVBoxLabelAndText(labelName, txtName);

        labelPassword = createCustomLabel("Password:");
        txtPassword = createCustomPasswordField("Enter Password!");
        vBoxPassword = createCustomVBoxLabelAndText(labelPassword, txtPassword);


        labelConfirmPassword = createCustomLabel("Confirm Password:");
        txtConfirmPassword = createCustomPasswordField("Enter Confirm Password!");
        vBoxConfirmPassword = createCustomVBoxLabelAndText(labelConfirmPassword, txtConfirmPassword);

        labelGender = createCustomLabel("Gender:");
        cmbGender = new ComboBox<>();
        cmbGender.getItems().addAll("Male", "Female", "Other");
        cmbGender.setPromptText("Select Gender!");
        cmbGender.setPrefWidth(240);
        cmbGender.setPrefHeight(30);

        vBoxComboBox = createCustomVBoxComboBox(labelGender, cmbGender);

        labelDOB = createCustomLabel("Date of Birth:");
        // DOB
        datePicker = new DatePicker(); //Cho người dùng chọn ngày
        datePicker.setMaxWidth(240);
        datePicker.setMaxHeight(30);

        LocalDate getValueFormDatePicker = datePicker.getValue(); //Giá trị ngày lấy từ DatePicker

        // Định dạng thành chuỗi kiểu "dd/mm/yy"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // Gán sự kiện khi người dùng chọn ngày mới
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String formattedDate = newValue.format(formatter);
                System.out.println("Ngày được chọn: " + formattedDate);
            }
        });

        vBoxDOB = createCustomDOB(labelDOB, datePicker);

        vBoxNameAndPass.getChildren().addAll(vBoxName, vBoxPassword, vBoxConfirmPassword,vBoxComboBox,vBoxDOB);


        btnLogUp = createCustomButton("Log Up");
        btnLogUp.setOnAction(event -> {
            openLogInWindow(currentStage);
        });
        effectMouse(btnLogUp);
        root.getChildren().addAll(vBoxNameAndPass,btnLogUp);
    }

    public void effectMouse(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(0.9);
            scaleDown.setToY(0.9);
            scaleDown.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1);
            scaleUp.setToY(1);
            scaleUp.play();
        });
        button.setOnMouseExited(e -> {
            button.setBackground(new Background(new BackgroundFill(Color.LIGHTSKYBLUE, new CornerRadii(20), null)));
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        button.setOnMouseEntered(e -> {
            button.setBackground(new Background(new BackgroundFill(Color.web("#126180"), new CornerRadii(20), null)));
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
    }

    private void openLogInWindow(Stage currentStage) {
        String userName = txtName.getText().trim();
        String password = txtPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();
        String gender = cmbGender.getValue(); // Lấy giá trị giới tính
        LocalDate dob = datePicker.getValue(); // Lấy ngày sinh

        if (password.equals(confirmPassword)) {
            // Kiểm tra từng trường riêng lẻ
            if (userName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "/IconInfor.png", "Please enter user!");
            } else if (password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "/IconInfor.png", "Please enter password!");
            } else if (confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "/IconInfor.png", "Please enter confirm password!");
            } else if (gender == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "/IconInfor.png", "Please select gender!");
            } else if (dob == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "/IconInfor.png", "Please select date of birth!");
            } else if(DAOUser.getInstance().isUsernameExist(userName)) {
                showAlert(Alert.AlertType.ERROR, "Error", "/IconError.png", "Username " + userName + " existed. Please enter the different username!");
            } else {
                // Nếu tất cả các trường hợp lệ, tiến hành đăng ký
                UserModel userModel = new UserModel(userName, password, confirmPassword,gender,dob);
                DAOUser.getInstance().insert(userModel);
                showAlert(Alert.AlertType.INFORMATION, "Success", "/IconSuccess.png", "Account has been registered successfully!");

                // Mở cửa sổ đăng nhập
                try {
                    LogIn logInView = new LogIn();
                    Stage logInStage = new Stage();
                    logInView.start(logInStage);

                    // Đóng cửa sổ hiện tại
                    if (currentStage != null) {
                        currentStage.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "/IconError.png", "Unable to open log up window!");
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "/IconError.png", "Passwords do not match. Please try again!");
        }
    }

    private TextField createCustomTextField(String prompt){
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setMaxWidth(240);
        textField.setMinWidth(240);
        textField.setPrefHeight(30);

        Platform.runLater(() -> {
            textField.getParent().requestFocus();
        });
        return textField;
    }

    private PasswordField createCustomPasswordField(String prompt) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(prompt);
        passwordField.setMaxWidth(240);
        passwordField.setMinWidth(240);
        passwordField.setPrefHeight(30);

        Platform.runLater(() -> {
            passwordField.getParent().requestFocus();
        });

        return passwordField;
    }


    private Label createCustomLabel(String labelText){
        Label label = new Label(labelText);
        label.setFont(Font.font("Times New Roman", FontWeight.BOLD, 20));
        return label;
    }

    private Button createCustomButton(String prompt){
        Button button = new Button(prompt);
        button.setBackground(new Background(new BackgroundFill(Color.LIGHTSKYBLUE, new CornerRadii(20), new Insets(5))));
        button.setFont(Font.font("Times New Roman", FontWeight.BOLD, 20));
        button.setPrefSize(300,50);
        button.setMinSize(300, 50);
        button.setMaxSize(300, 50);
        return button;
    }

    private VBox createCustomVBoxLabelAndText(Label label, TextField textField){
        VBox root = new VBox(5); // Khoảng cách 5px giữa label và text field
        root.setAlignment(Pos.CENTER); // Căn giữa các thành phần
        root.setPadding(new Insets(5)); // Padding 5px cho VBox

        VBox vBox = new VBox(5);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setPadding(new Insets(5));
        vBox.getChildren().addAll(label, textField);

        root.getChildren().add(vBox);
        return root;
    }

    private VBox createCustomVBoxComboBox(Label label, ComboBox<String> comboBox){
        VBox root = new VBox(5);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(5));

        VBox vBox = new VBox(5);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().addAll(label, comboBox);

        root.getChildren().add(vBox);
        return root;
    }

    private VBox createCustomDOB(Label label, DatePicker datePicker){
        VBox root = new VBox(5);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(5));

        VBox vBox = new VBox(5);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().addAll(label, datePicker);

        root.getChildren().add(vBox);
        return root;
    }

    private void showAlert(Alert.AlertType alertType, String title, String iconPath, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.setHeaderText(null);
        // Đặt icon cho alert
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream(iconPath)));
        alert.showAndWait();
    }
}
