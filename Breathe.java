package Breathe;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Breathe extends Application {

    @Override
    public void start(Stage primaryStage) {

        Circle circle1 = new Circle(150);
        circle1.setFill(Color.web("#d0ebf2"));

        Circle circle2 = new Circle(100);
        circle2.setFill(Color.web("#158fad"));

        Text text = new Text("Inhale");
        text.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: #ffffff;");

        ScaleTransition scaleTransition1 = new ScaleTransition(Duration.seconds(5), circle1);
        scaleTransition1.setFromX(0.8);
        scaleTransition1.setFromY(0.8);
        scaleTransition1.setToX(1.5);
        scaleTransition1.setToY(1.5);
        scaleTransition1.setCycleCount(ScaleTransition.INDEFINITE);
        scaleTransition1.setAutoReverse(true);
        scaleTransition1.play();


        ScaleTransition scaleTransition2 = new ScaleTransition(Duration.seconds(7), circle2);
        scaleTransition2.setFromX(1.0);
        scaleTransition2.setFromY(1.0);
        scaleTransition2.setToX(1.3);
        scaleTransition2.setToY(1.3);
        scaleTransition2.setCycleCount(ScaleTransition.INDEFINITE);
        scaleTransition2.setAutoReverse(true);
        scaleTransition2.play();


        Timeline textTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> text.setText("Inhale")),
                new KeyFrame(Duration.seconds(10), e -> text.setText("Exhale")),
                new KeyFrame(Duration.seconds(20), e -> text.setText("Inhale"))
        );
        textTimeline.setCycleCount(Timeline.INDEFINITE);
        textTimeline.play();


        StackPane centerPane = new StackPane(circle1, circle2, text);
        centerPane.setAlignment(Pos.CENTER);


        BorderPane root = new BorderPane();
        root.setCenter(centerPane);
        root.setBackground(new Background(new BackgroundFill(Color.SEASHELL, new CornerRadii(30), new Insets(10))));

        Image imageBreathe = new Image(getClass().getResourceAsStream("/IconsBreathe.png"));
        primaryStage.getIcons().add(imageBreathe);

        Scene scene = new Scene(root, 500, 500);
        primaryStage.setTitle("Breathe App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}

