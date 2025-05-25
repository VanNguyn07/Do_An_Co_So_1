package Space;

import ViewDACS.ViewMain;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class SpaceApp extends Application {
    private Stage currentStage;
    private MediaPlayer currentMediaPlayer;

    @Override
    public void start(Stage stage) {
        currentStage = stage; // Gán Stage hiện tại vào biến instance

        // Tạo GridPane để chứa các thẻ không gian
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(15));
        gridPane.setAlignment(Pos.TOP_CENTER);

        // Danh sách các không gian và đường dẫn video
        String[] spaces = {
                "Nature Sea", "Seattle", "Mount Shuksan",
                "Rain Forest", "Lotus Lake", "Sand", "Sunrise", "Sunset", "Flower"
        };
        String[] videoPaths = {
                "/nature.mp4", "/seattle.mp4", "/MountShuksan.mp4",
                "/rainForest.mp4", "/LotusLake.mp4", "/sand.mp4", "/sunrise.mp4", "/sunset.mp4","/flower.mp4"
        };

        // Tạo các thẻ cho từng không gian
        for (int i = 0; i < spaces.length; i++) {
            VBox card = createSpaceCard(spaces[i], videoPaths[i]);
            gridPane.add(card, i % 3, i / 3); // 3 cột trên mỗi hàng
        }

        // ScrollPane để hỗ trợ cuộn
        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Lớp chứa ScrollPane với nền và bo góc
        StackPane container = new StackPane(scrollPane);
        container.setBackground(new Background(new BackgroundFill(Color.SEASHELL, new CornerRadii(20), new Insets(0))));

        // Clip động theo kích thước container
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(container.widthProperty());
        clip.heightProperty().bind(container.heightProperty());
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        container.setClip(clip);

        // Scene và Stage
        Scene scene = new Scene(container, 350, 500);
        stage.setScene(scene);
        stage.setTitle("Space App");
        stage.setMinWidth(350);
        stage.setMinHeight(500);
        initIconMain(stage);
        stage.show();
    }

    // Phương thức tạo thẻ không gian với video
    private VBox createSpaceCard(String title, String videoPath) {
        VBox card = new VBox(10);
        card.setPrefSize(100, 150);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Tạo MediaView để phát video
        try {
            Media media = new Media(getClass().getResource(videoPath).toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true); // Tự động phát
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Lặp vô hạn
            mediaPlayer.setMute(true); // Tắt tiếng (tùy chọn)

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(100); // Điều chỉnh kích thước video
            mediaView.setFitHeight(120);

            // Nhãn tiêu đề
            Label label = new Label(title);
            label.setWrapText(true);
            label.setStyle("-fx-text-fill: black; -fx-font-size: 12;");

            // Biểu tượng trái tim
            Label heart = new Label("♡");
            heart.setStyle("-fx-text-fill: black; -fx-font-size: 16; -fx-cursor: hand;"); // Màu mặc định là đen
            heart.setOnMouseClicked(e -> {
                if (heart.getText().equals("♡")) {
                    heart.setText("♥");
                    heart.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-cursor: hand;"); // Đổi sang đỏ khi là ♥
                } else {
                    heart.setText("♡");
                    heart.setStyle("-fx-text-fill: black; -fx-font-size: 16; -fx-cursor: hand;"); // Đổi lại đen khi là ♡
                }
                e.consume(); // Ngăn sự kiện chuột lan tỏa đến card
            });

            card.getChildren().addAll(mediaView, label, heart);
            // Thay đổi video nền và đóng cửa sổ
            card.setOnMouseClicked(e -> {
                String videoUrl = getClass().getResource(videoPath).toExternalForm();
                ViewMain.updateBackgroundVideo(videoUrl); // Cập nhật video nền trong ViewMain
                ViewMain.resetSpacesButton(); // Reset trạng thái nút Spaces
                if (currentStage != null) currentStage.close(); // Đóng cửa sổ SpaceApp
            });

        } catch (Exception e) {
            // Xử lý lỗi nếu không tìm thấy file video
            Label errorLabel = new Label("Video not found: " + title);
            card.getChildren().add(errorLabel);
            System.err.println("Error loading video: " + e.getMessage());
        }

        return card;
    }

    public void initIconMain(Stage stage) {
        Image imageSpace = new Image(getClass().getResourceAsStream("/IconsSpaces.png"));
        stage.getIcons().add(imageSpace);
    }

    public static void main(String[] args) {
        launch(args);
    }
}