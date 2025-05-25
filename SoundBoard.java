package Sound;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SoundBoard extends Application {

    public MediaPlayer mediaPlayer;
    private Label labelSoundBoard;
    private TextField textFieldURL;
    private Button buttonLoad, buttonTransparent, buttonPlayAndPause;
    private Slider volumeSlider, seekSlider;
    private ImageView iconPlayAndPause;
    private ComboBox<String> comboBoxSound;
    private Map<String, String> soundMap;
    private Label loadingLabel;
    private String currentComboBoxSong; // Lưu bài hát hiện tại từ ComboBox

    @Override
    public void start(Stage stage) throws Exception {
        VBox vboxMain = new VBox(15);
        vboxMain.setPrefWidth(420);
        vboxMain.setPrefHeight(310);
        vboxMain.setAlignment(Pos.CENTER);
        vboxMain.setBackground(new Background(new BackgroundFill(Color.SEASHELL, new CornerRadii(20), new Insets(5))));

        labelSoundBoard = new Label("Sound Board");
        labelSoundBoard.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 40));

        vboxMain.getChildren().add(labelSoundBoard);
        initLabelAndButtonURL(vboxMain);

        Scene scene = new Scene(vboxMain);
        stage.setTitle("Sound App");
        stage.setScene(scene);
        intiIconSound(stage);
        stage.show();

        stage.setOnCloseRequest(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
        });

        Platform.runLater(() -> {
            textFieldURL.getParent().requestFocus();
        });
    }

    public void intiIconSound(Stage stage) {
        Image imageSound = new Image(getClass().getResourceAsStream("/IconsSound.png"));
        stage.getIcons().add(imageSound);
    }

    public void initLabelAndButtonURL(VBox vBox) {
        textFieldURL = new TextField();
        textFieldURL.setPromptText("Add .mp3 audio URL or available sounds! ");
        textFieldURL.setPrefWidth(240);

        buttonLoad = new Button("UpLoad");
        buttonLoad.setBackground(new Background(new BackgroundFill(Color.LIGHTSKYBLUE, new CornerRadii(10), Insets.EMPTY)));
        buttonLoad.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));

        effectMouse(buttonLoad);

        buttonTransparent = new Button("");
        buttonTransparent.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(15), Insets.EMPTY)));
//        buttonReset.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));
//        effectMouse(buttonReset);

        // Danh sách bài hát
        soundMap = new LinkedHashMap<>();
        soundMap.put("Cay - Khắc Hưng, Jimmi Nguyễn", "file:/C:/Music/Cay.mp3");
        soundMap.put("01 Ngoại Lệ - Jack", "file:/C:/Music/01NgoaiLe.mp3");
        soundMap.put("Anh Chẳng Thể - Phạm Kỳ", "file:/C:/Music/AnhChangThe.mp3");
        soundMap.put("Má Em Chê Anh Nghèo (Remix)", "file:/C:/Music/MaEmCheAnhNgheo.mp3");
        soundMap.put("Ôm Em Thật Lâu - MONO", "file:/C:/Music/OmEmThatLau.mp3");
        soundMap.put("Nước Mắt Cá Sấu - HTH", "file:/C:/Music/NuocMatCaSau.mp3");
        soundMap.put("Trình - HTH", "file:/C:/Music/Trinh.mp3");
        soundMap.put("8 Vạn 6 Ngàn Thương (Remix)", "file:/C:/Music/8Van6NganThuong.mp3");
        soundMap.put("BIGTEAM", "file:/C:/Music/BigTeam.mp3");
        soundMap.put("Sự Nghiệp Chướng - Pháo", "file:/C:/Music/SuNghiepChuong.mp3");
        soundMap.put("Dù Cho Tận Thế - Erik", "file:/C:/Music/DuChoTanThe.mp3");
        soundMap.put("Mất Kết Nối - DOMIC", "file:/C:/Music/MatKetNoi.mp3");
        soundMap.put("Eyes,Nose,Lips - Tae Yang","file:/C:/Music/EyesNoseLips.mp3");

        // Tạo ComboBox
        comboBoxSound = new ComboBox<>();
        comboBoxSound.getItems().addAll(soundMap.keySet());
        comboBoxSound.setPromptText("Select available sounds!");
        comboBoxSound.setVisibleRowCount(5); // Chỉ hiển thị 5 hàng, có scroll nếu nhiều hơn
        comboBoxSound.prefWidthProperty().bind(textFieldURL.prefWidthProperty());

        // Khi chọn bài từ ComboBox
        comboBoxSound.setOnAction(event -> {
            String selected = comboBoxSound.getValue();
            if (selected != null) {
                textFieldURL.setText("");
                comboBoxSound.setValue(selected); // Đặt giá trị ComboBox thành bài hát đã chọn
                currentComboBoxSong = selected; // Lưu bài hát hiện tại
                playSound(soundMap.get(selected), selected, true); // true: từ ComboBox
            }
        });

        // Khi nhấn Enter trong TextField
        textFieldURL.setOnAction(event -> {
            String input = textFieldURL.getText().trim();
            if (input.isEmpty()) {
                alertInformation();
                return;
            }
            if (soundMap.containsKey(input)) {
                playSound(soundMap.get(input), input, true); // true: từ ComboBox
                comboBoxSound.setValue(input); // Cập nhật ComboBox
                currentComboBoxSong = input; // Lưu bài hát hiện tại
            } else {
                playSound(input, extractSongName(input, input), false); // false: từ textFieldURL
            }
        });

        // Khi nhấn nút UpLoad
        buttonLoad.setOnAction(e -> {
            String input = textFieldURL.getText().trim();
            if (input.isEmpty()) {
                alertInformation();
                return;
            }
            if (soundMap.containsKey(input)) {
                playSound(soundMap.get(input), input, true); // true: từ ComboBox
                comboBoxSound.setValue(input); // Cập nhật ComboBox
                currentComboBoxSong = input; // Lưu bài hát hiện tại
            } else {
                playSound(input, extractSongName(input, input), false); // false: từ textFieldURL
            }
        });

        HBox hbox = new HBox(10, textFieldURL, buttonLoad);
        hbox.setAlignment(Pos.CENTER);

        HBox comboBoxContainer = new HBox(90,comboBoxSound, buttonTransparent);
//        comboBoxContainer.setPadding(new Insets(0, 0, 0, 1));
        comboBoxContainer.setAlignment(Pos.CENTER);

        VBox vboxUploadAndCombo = new VBox(10);
        vboxUploadAndCombo.setAlignment(Pos.CENTER);
        vboxUploadAndCombo.getChildren().addAll(hbox, comboBoxContainer);

        vBox.getChildren().add(vboxUploadAndCombo);
        initButtonPlayAndSlider(buttonLoad, vBox);
    }

    private void playSound(String url, String soundName, boolean fromComboBox) {
        // Kiểm tra URL trước khi dừng mediaPlayer
        boolean isValidUrl = true;

        // Loại bỏ query string để kiểm tra định dạng
        String baseUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        try {
            // Kiểm tra đơn giản: URL cơ bản phải kết thúc bằng .mp3 nếu không phải từ ComboBox
            if (!fromComboBox && !baseUrl.toLowerCase().endsWith(".mp3")) {
                isValidUrl = false;
            } else {
                // Thử tạo Media để kiểm tra URL thực sự hợp lệ
                Media media = new Media(url); // Sử dụng url đầy đủ, bao gồm query string
            }
        } catch (Exception e) {
            isValidUrl = false;
        }

        // Nếu URL không hợp lệ, hiển thị thông báo và giữ nguyên bài hát hiện tại
        if (!isValidUrl) {
            Platform.runLater(() -> {
                loadingLabel.setText("");
                buttonLoad.setDisable(false);
                alertError();
                textFieldURL.setText("");
                // Không dừng mediaPlayer, giữ nguyên bài hát hiện tại
                if (mediaPlayer != null && fromComboBox) {
                    // Nếu bài hát hiện tại là từ ComboBox, tiếp tục phát
                    mediaPlayer.play();
                    iconPlayAndPause.setImage(new Image(getClass().getResourceAsStream("/IconPause.png")));
                }
            });
            return;
        }

        // Nếu URL hợp lệ, tiến hành dừng mediaPlayer hiện tại và phát bài mới
        Platform.runLater(() -> {
            loadingLabel.setText("Loading...");
            buttonLoad.setDisable(true);
            iconPlayAndPause.setImage(new Image(getClass().getResourceAsStream("/IconPlay.png")));
        });

        // Dừng mediaPlayer hiện tại ngay lập tức
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.volumeProperty().unbind();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        try {
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                handleSuccessfulLoad(soundName, fromComboBox);
            });

            mediaPlayer.setOnError(() -> {
                Platform.runLater(() -> {
                    loadingLabel.setText("");
                    buttonLoad.setDisable(false);
                    alertError();
                    buttonPlayAndPause.setDisable(true);
                    seekSlider.setDisable(true);
                    volumeSlider.setDisable(true);
                    textFieldURL.setText("");
                    currentComboBoxSong = null; // Xóa bài hát hiện tại nếu lỗi
                });
            });

        } catch (Exception e) {
            Platform.runLater(() -> {
                loadingLabel.setText("");
                buttonLoad.setDisable(false);
                alertError();
                buttonPlayAndPause.setDisable(true);
                seekSlider.setDisable(true);
                volumeSlider.setDisable(true);
                textFieldURL.setText("");
                currentComboBoxSong = null; // Xóa bài hát hiện tại nếu lỗi
            });
        }
    }

    private void handleSuccessfulLoad(String soundName, boolean fromComboBox) {
        Platform.runLater(() -> {
            EventHandler<ActionEvent> oldTextFieldHandler = comboBoxSound.getOnAction();
            textFieldURL.setOnAction(null);// Tạm vô hiệu hóa sự kiện textFieldURL

            buttonPlayAndPause.setDisable(false);
            seekSlider.setDisable(false);
            volumeSlider.setDisable(false);
            seekSlider.setValue(0);

            mediaPlayer.play();
            iconPlayAndPause.setImage(new Image(getClass().getResourceAsStream("/IconPause.png")));
            loadingLabel.setText("");
            buttonLoad.setDisable(false);
            if (!fromComboBox) {
                alertSuccess();
            }

            // Đặt lại ComboBox khi tải từ textFieldURL
            if (!fromComboBox) {
                EventHandler<ActionEvent> oldComboHandler = comboBoxSound.getOnAction();
                comboBoxSound.setOnAction(null); // Tạm vô hiệu hóa sự kiện ComboBox
                resetComboBox();
                comboBoxSound.setOnAction(oldComboHandler);// Khôi phục sự kiện
            }

            textFieldURL.setOnAction(oldTextFieldHandler);// Khôi phục sự kiện
        });

        // Liên kết volumeProperty với valueProperty của volumeSlider
        mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty());

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (!seekSlider.isPressed() && !seekSlider.isValueChanging() && mediaPlayer != null) {
                    double duration = mediaPlayer.getTotalDuration().toSeconds();
                    if (duration > 0) {
                        seekSlider.setValue(newValue.toSeconds() / duration);
                    }
                }
            });
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            Platform.runLater(() -> {
                seekSlider.setValue(0);
                iconPlayAndPause.setImage(new Image(getClass().getResourceAsStream("/IconPlay.png")));

                if (soundMap.containsKey(soundName)) {
                    List<String> soundNames = new ArrayList<>(soundMap.keySet());
                    int currentIndex = soundNames.indexOf(soundName);
                    int nextIndex = (currentIndex + 1) % soundNames.size();
                    String nextSound = soundNames.get(nextIndex);
//                    textFieldURL.setText("");
                    // Tạm vô hiệu hóa sự kiện để tránh ghi đè
                    EventHandler<ActionEvent> oldComboHandler = comboBoxSound.getOnAction();
                    comboBoxSound.setOnAction(null);
                    comboBoxSound.setValue(nextSound);
                    currentComboBoxSong = nextSound;
                    playSound(soundMap.get(nextSound), nextSound, true); // true: từ ComboBox
                }
            });
        });

        seekSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (seekSlider.isValueChanging() && mediaPlayer != null) {
                    double duration = mediaPlayer.getTotalDuration().toSeconds();
                    if (duration > 0) {
                        mediaPlayer.seek(Duration.seconds(newValue.doubleValue() * duration));
                    }
                }
            });
        });

        seekSlider.setOnMousePressed(event -> {
            Platform.runLater(() -> {
                if (mediaPlayer != null) {
                    double duration = mediaPlayer.getTotalDuration().toSeconds();
                    if (duration > 0) {
                        mediaPlayer.seek(Duration.seconds(seekSlider.getValue() * duration));
                    }
                }
            });
        });
    }

    public void initButtonPlayAndSlider(Button button, VBox vBox) {
        iconPlayAndPause = new ImageView(new Image(getClass().getResourceAsStream("/IconPlay.png")));
        iconPlayAndPause.setFitWidth(30);
        iconPlayAndPause.setFitHeight(30);

        buttonPlayAndPause = new Button("", iconPlayAndPause);
        buttonPlayAndPause.setBackground(new Background(new BackgroundFill(Color.LIGHTSKYBLUE, new CornerRadii(15), Insets.EMPTY)));
        buttonPlayAndPause.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));
        buttonPlayAndPause.setDisable(true);
        effectMouse(buttonPlayAndPause);

        loadingLabel = new Label("");
        loadingLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 17));

        seekSlider = new Slider(0, 1, 0);
        seekSlider.setShowTickMarks(false);
        seekSlider.setShowTickLabels(false);
        seekSlider.setDisable(true);

        Label volumeLabel = new Label("Volume:");
        volumeLabel.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));

        volumeSlider = new Slider(0.0, 1.0, 0.5);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setMinorTickCount(4);
        volumeSlider.setBlockIncrement(0.1);
        volumeSlider.setDisable(true);
        volumeSlider.setPrefWidth(385);

// Tùy chỉnh màu cho volumeSlider, bao gồm cả gạch chia và nhãn
        volumeSlider.setStyle(
                "-fx-control-inner-background: #87CEFA;" +  // Màu nền: xanh lam nhạt
                        "-fx-track-color: #87CEFA;" +               // Thanh trượt: xanh dương nhạt
                        "-fx-thumb-color: #0000FF;" +               // Nút điều chỉnh: xanh dương đậm
                        "-fx-tick-mark-fill: #FF4500;" +            // Màu gạch chia: đỏ cam
                        "-fx-tick-label-fill: #FF0000;" +
                        "-fx-tick-length: 8;"// Màu nhãn gạch chia: đỏ đậm
        );

        volumeSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double aDouble) {
                return String.format("%.0f%%", aDouble * 100);
            }

            @Override
            public Double fromString(String s) {
                return Double.valueOf(s.replace("%", "")) / 100;
            }
        });

        button.setOnAction(e -> {
            String input = textFieldURL.getText().trim();
            if (input.isEmpty()) {
                alertInformation();
                return;
            }
            if (soundMap.containsKey(input)) {
                playSound(soundMap.get(input), input, true); // true: từ ComboBox
                comboBoxSound.setValue(input); // Cập nhật ComboBox
                currentComboBoxSong = input; // Lưu bài hát hiện tại
            } else {
                playSound(input, extractSongName(input, input), false); // false: từ textFieldURL
            }
        });

        buttonPlayAndPause.setOnAction(e -> {
            Platform.runLater(() -> {
                if (mediaPlayer == null) {
                    return;
                }
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    iconPlayAndPause.setImage(new Image(getClass().getResourceAsStream("/IconPlay.png")));
                } else {
                    mediaPlayer.play();
                    iconPlayAndPause.setImage(new Image(getClass().getResourceAsStream("/IconPause.png")));
                }
            });
        });

        HBox hBoxSeekSlider_PlayPause = new HBox(10, seekSlider, buttonPlayAndPause);
        hBoxSeekSlider_PlayPause.setAlignment(Pos.CENTER);

        HBox hBoxVolume = new HBox(volumeSlider);
        hBoxVolume.setAlignment(Pos.CENTER);

        VBox vBoxVolumeSliderAndLabel = new VBox(18, volumeLabel, hBoxVolume);
        vBoxVolumeSliderAndLabel.setAlignment(Pos.CENTER);

        VBox vBox_Volumelabel_HBoxVolume = new VBox(hBoxSeekSlider_PlayPause, vBoxVolumeSliderAndLabel, loadingLabel);
        vBox_Volumelabel_HBoxVolume.setAlignment(Pos.CENTER);
        vBox_Volumelabel_HBoxVolume.setPadding(new Insets(8, 0, 0, 0));
        vBox.getChildren().addAll(vBox_Volumelabel_HBoxVolume);
    }

    private String extractSongName(String url, String fallbackName) {
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            if (fileName.toLowerCase().endsWith(".mp3")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            if (fileName.matches("[a-zA-Z0-9]{8,12}")) {
                return fallbackName;
            }
            fileName = fileName.replaceAll("-\\d+$", "");
            fileName = fileName.replaceAll("-", " ");
            return fileName.trim();
        } catch (Exception e) {
            return fallbackName;
        }
    }

    private void alertInformation() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("The .mp3 URL is empty. Please enter!");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/IconInfor.png")));
        alert.showAndWait();
    }

    private void alertError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Invalid .mp3 URL!");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/IconError.png")));
        alert.showAndWait();
    }
    private void alertSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Valid .mp3 URL and ready to play!");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/IconSuccess.png")));
        alert.showAndWait();
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
            button.setBackground(new Background(new BackgroundFill(Color.LIGHTSKYBLUE, new CornerRadii(10), null)));
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        button.setOnMouseEntered(e -> {
            button.setBackground(new Background(new BackgroundFill(Color.web("#126180"), new CornerRadii(10), null)));
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
    }

    private void resetComboBox() {
        Platform.runLater(() -> {
            EventHandler<ActionEvent> oldHandler = comboBoxSound.getOnAction();
            comboBoxSound.setOnAction(null);// Tạm vô hiệu hóa sự kiện
            comboBoxSound.getSelectionModel().clearSelection();
            comboBoxSound.setValue(null);
            comboBoxSound.setPromptText("Select available sounds!");
            comboBoxSound.applyCss();// Áp dụng lại CSS
            comboBoxSound.layout();// Làm mới bố cục
            System.out.println("ComboBox value after reset: " + comboBoxSound.getValue());
            System.out.println("ComboBox prompt text: " + comboBoxSound.getPromptText());
            System.out.println("ComboBox is showing: " + comboBoxSound.isShowing());
            currentComboBoxSong = null;
            comboBoxSound.setOnAction(oldHandler);// Khôi phục sự kiện
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}