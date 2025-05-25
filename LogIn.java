package LogIn;

import LogUp.LogUp;
import Model.AccountManagement;
import ViewDACS.ViewMain;
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
import java.util.ArrayList;

public class LogIn extends Application {
    private Label labelName, labelPassword,labelGender;
    private TextField txtName;
    private PasswordField txtPassword;
    private ComboBox<String> cmbGender;
    private Button btnLogIn;
    private VBox vBoxName = new VBox();
    private VBox vBoxPassword = new VBox();
    private VBox vBoxComboBox = new VBox();
    private VBox vBoxNameAndPass = new VBox();
    private Stage currentStage;
    public void start(Stage stage) throws Exception {
            this.currentStage = stage;// Lưu Stage hiện tại

            // Tạo HBox làm container chính để điều chỉnh vị trí ngang
            HBox rootContainer = new HBox(); // Khoảng cách giữa các thành phần (nếu có)
            rootContainer.setAlignment(Pos.CENTER); // Căn giữa theo chiều dọc
            rootContainer.setPadding(new Insets(20, 20, 20, 30)); // Thêm padding trái 50px để đẩy sang phải
            rootContainer.setBackground(new Background(new BackgroundFill(Color.SEASHELL, new CornerRadii(20), new Insets(5))));

            // Tạo VBox cho nội dung chính
            VBox root = new VBox(10);
            root.setPrefSize(400,300);
            root.setAlignment(Pos.CENTER);

            Label labelLogUp = new Label("Log In");
            labelLogUp.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 40));
            root.getChildren().add(labelLogUp);

            initLabelAndTextField(root);

            // Thêm root vào HBox
            rootContainer.getChildren().add(root);

            Scene scene = new Scene(rootContainer);
            stage.setTitle("Log In");
            stage.setScene(scene);
            initIconLogUp(stage);
            stage.show();
        }

        private void initIconLogUp(Stage stage) {
            Image imageLogUp = new Image(getClass().getResourceAsStream("/IconsLogIn.png"));
            stage.getIcons().add(imageLogUp);
        }

        private void initLabelAndTextField(VBox root) {

            labelName = createCustomLabel("User Name:");
            txtName = createCustomTextField("Enter Full Name!");
            vBoxName = createCustomVBoxLabelAndText(labelName, txtName);

            labelPassword = createCustomLabel("Password:");
            txtPassword = createCustomPasswordField("Enter Password!");
            vBoxPassword = createCustomVBoxLabelAndText(labelPassword, txtPassword);



            labelGender = createCustomLabel("Gender:");
            cmbGender = new ComboBox<>();
            cmbGender.getItems().addAll("Male", "Female", "Other");
            cmbGender.setPromptText("Select Gender!");
            cmbGender.setPrefWidth(240);
            cmbGender.setPrefHeight(30);

            vBoxComboBox = createCustomVBoxComboBox(labelGender, cmbGender);


            vBoxNameAndPass.getChildren().addAll(vBoxName, vBoxPassword ,vBoxComboBox);


            btnLogIn = createCustomButton("Log In");
            btnLogIn.setOnAction(event -> {
                if(txtName.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error","/IconError.png","Please enter Username!");
                }else if(txtPassword.getText().isEmpty()){
                    showAlert(Alert.AlertType.ERROR, "Error","/IconError.png","Please enter Password!");
                }else if(cmbGender.getValue() == null){
                    showAlert(Alert.AlertType.ERROR, "Error","/IconError.png","Please select Gender!");
                } else {
                    callViewMain(currentStage);
                }
            });
            effectMouse(btnLogIn);

            // Thêm Hyperlink cho đăng ký
            Hyperlink registerLink = new Hyperlink("Don't have an account? Log up here");
            registerLink.setStyle("-fx-text-fill: black; -fx-underline: true; -fx-font-size: 14px;");
            registerLink.setOnAction(e -> openRegistrationWindow(currentStage));

            registerLink.setOnMouseEntered(event -> {
                registerLink.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-font-size: 14px;");
            });

            registerLink.setOnMouseExited(event -> {
               registerLink.setStyle("-fx-text-fill: black; -fx-underline: true; -fx-font-size: 14px;");
            });

            // Thêm Hyperlink vào giao diện
            VBox linkBox = new VBox(10, btnLogIn, registerLink);
            linkBox.setAlignment(Pos.CENTER);

            root.getChildren().addAll(vBoxNameAndPass, linkBox);
        }

    private void openRegistrationWindow(Stage currentStage) {
        try {
            LogUp registrationView = new LogUp(); // Tái sử dụng class LogUp cho đăng ký
            Stage registrationStage = new Stage();
            registrationView.start(registrationStage); // Mở cửa sổ đăng ký mới

            if(currentStage != null) {
                currentStage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error","/IconError.png","Unable to open Log up window:");
        }
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

    public boolean callViewMain(Stage currentStage){
        String userName = txtName.getText().trim();
        String password = txtPassword.getText().trim();
        String gender = cmbGender.getValue().trim();

        AccountManagement acc = new AccountManagement();// tạo mới đối tượng AccountManager để use phương thức getInstance
        ArrayList<String> arrayList = acc.getInstance(userName, password, gender);

        String userNameFromCSDL = acc.getDataFromCSDL(userName);

        if(!arrayList.isEmpty() || arrayList.size() >= 2){

            if(userNameFromCSDL != null) {
                // Kiểm tra chữ hoa/thường
                boolean hasCaseDifference = compare(userName, userNameFromCSDL);
                if (!hasCaseDifference) {
                    System.out.println("Success: Username hợp lệ, mở ViewMain");
                    showAlert(Alert.AlertType.INFORMATION, "Success", "/IconSuccess.png","Account created successfully!");

                    // Mở giao diện ViewMain
                    try {
                        ViewMain viewMain = new ViewMain();
                        Stage stage = new Stage();
                        viewMain.start(stage);

                        // Đóng cửa sổ hiện tại (LogIn)
                        if (currentStage != null) {
                            currentStage.close();
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "/IconError.png", "Unable to ViewMain!");
                        txtName.setText("");
                        txtPassword.setText("");
                        cmbGender.setValue("Select Gender!");
                        return false;
                    }
                } else {
                    // Có sự khác biệt về chữ hoa/thường
                    showAlert(Alert.AlertType.ERROR, "Error", "/IconError.png",
                            "Username invalid: Wrong uppercase/lowercase!");
                    txtName.setText("");
                    txtPassword.setText("");
                    cmbGender.setValue("Select Gender!");
                    return false;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "/IconError.png","Unable to create account. Please check information again!");
                txtName.setText("");
                txtPassword.setText("");
                cmbGender.setValue("Select Gender!");
                return false;
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "/IconError.png","Unable to create account. Please check information again!");
            txtName.setText("");
            txtPassword.setText("");
            cmbGender.setValue("Select Gender!");
            return false;
        }
    }

    private boolean compare(String nameTextField, String nameCSDL){
        StringBuilder result = new StringBuilder();
        result.append("So sánh: '").append(nameTextField).append("' với '").append(nameCSDL).append("'\n");

        // Kiểm tra độ dài chuỗi
        if (nameTextField.length() != nameCSDL.length()) {
            result.append("Hai chuỗi có độ dài khác nhau!\n");
            System.out.println(result);
            return true; // Có sự khác biệt
        }

        boolean hasCaseDifference = false;
        for (int i = 0; i < nameTextField.length(); i++) {
            char inputChar = nameTextField.charAt(i);
            char dbChar = nameCSDL.charAt(i);

            result.append("Vị trí ").append(i + 1).append(": ");
            result.append("'").append(inputChar).append("' (").append(getCase(inputChar)).append(") vs ");
            result.append("'").append(dbChar).append("' (").append(getCase(dbChar)).append(")\n");

            // Kiểm tra nếu khác nhau về chữ hoa/thường
            if (inputChar != dbChar && Character.toLowerCase(inputChar) == Character.toLowerCase(dbChar)) {
                result.append("  -> Khác nhau về chữ hoa/thường!\n");
                hasCaseDifference = true;
            }
        }

        System.out.println(result);
        return hasCaseDifference;
    }

    private String getCase(char c) {
        if (Character.isUpperCase(c)) {
            return "chữ hoa";
        } else if (Character.isLowerCase(c)) {
            return "chữ thường";
        } else {
            return "không phải chữ cái";
        }
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
