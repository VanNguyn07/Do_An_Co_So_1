package ViewDACS;

import Breathe.Breathe;
import Calendar.CalendarApp;
import LogUp.LogUp;
import Note.Notes;
import Sound.SoundBoard;
import Space.SpaceApp;
import Tasks.Tasks;
import Timer.Timer;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ViewMain extends Application {
    public Button btnTimer, btnNotes, btnSpaces, btnSound, btnTasks, btnBreathe, btnCal;
    public ImageView iconTimer, iconNotes, iconSpaces, iconSounds, iconTasks, iconBreathe, iconCal;
    public StackPane stackPane = new StackPane();
    private Stage stageTimer, stagecal, stageSound, stageBreathe, stageNotes, stageTasks, stageSpace;
    private SoundBoard soundBoard;
    private MediaPlayer currentBackgroundPlayer;
    private static ViewMain instance;

    public ViewMain() {
        instance = this;
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        stage.setTitle("Study timer management");

        stackPane.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT,
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        Scene scene = new Scene(stackPane, screenBounds.getWidth(), screenBounds.getHeight() - 30);
        stage.setScene(scene);

        initIconTitle(stage);
        HBox hbox = new HBox();
        hbox.setPrefHeight(100);
        createVideoBrackground();
        initSideBar(hbox);
        stackPane.getChildren().add(hbox);

        stage.show();
    }

    private void initIconTitle(Stage stageIcon) {
        Image iconTitle = new Image(getClass().getResourceAsStream("/IconsMain.png"));
        stageIcon.getIcons().add(iconTitle);
    }

    public void createVideoBrackground() {
        String defaultVideoPath = getClass().getResource("/Beach.mp4").toExternalForm();
        updateBackgroundVideo(defaultVideoPath);
    }

    public static void updateBackgroundVideo(String videoPath) {
        if (instance != null) {
            Platform.runLater(() -> {
                if (instance.currentBackgroundPlayer != null) {
                    instance.currentBackgroundPlayer.stop();
                    instance.stackPane.getChildren().removeIf(node -> node instanceof MediaView);
                }

                Media media = new Media(videoPath);
                instance.currentBackgroundPlayer = new MediaPlayer(media);
                instance.currentBackgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                instance.currentBackgroundPlayer.setAutoPlay(true);
                instance.currentBackgroundPlayer.setMute(true);

                MediaView mediaView = new MediaView(instance.currentBackgroundPlayer);
                mediaView.setPreserveRatio(false);
                mediaView.fitHeightProperty().bind(instance.stackPane.heightProperty());
                mediaView.fitWidthProperty().bind(instance.stackPane.widthProperty());

                instance.stackPane.getChildren().add(0, mediaView);
            });
        }
    }

    public static void resetSpacesButton() {
        if (instance != null) {
            Platform.runLater(() -> {
                instance.stageSpace = null;
                if (instance.btnSpaces != null) {
                    instance.btnSpaces.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                    instance.btnSpaces.setTextFill(Color.rgb(85, 85, 85));
                    instance.btnSpaces.setOpacity(1.0);
                }
            });
        }
    }

    public VBox effectSidebar(VBox vBoxSidebar) {
        PauseTransition hideSidebarTimer = new PauseTransition(Duration.seconds(5));
        hideSidebarTimer.setOnFinished(e -> {
            vBoxSidebar.setVisible(false);
        });

        stackPane.setOnMouseMoved(event -> {
            if (!vBoxSidebar.isVisible()) {
                vBoxSidebar.setVisible(true);
            }
            hideSidebarTimer.playFromStart();
        });

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), vBoxSidebar);
        slideIn.setFromX(-200);
        slideIn.setToX(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), vBoxSidebar);
        slideOut.setFromX(0);
        slideOut.setToX(-200);

        stackPane.setOnMouseMoved(event -> {
            if (!vBoxSidebar.isVisible()) {
                vBoxSidebar.setVisible(true);
                slideIn.playFromStart();
            }
            hideSidebarTimer.playFromStart();
        });

        hideSidebarTimer.setOnFinished(e -> {
            slideOut.playFromStart();
            slideOut.setOnFinished(ev -> vBoxSidebar.setVisible(false));
        });

        return vBoxSidebar;
    }

    public void initSideBar(HBox hbox) {
        VBox vboxSidebarMain = new VBox(30);
        vboxSidebarMain.setPadding(new Insets(9));
        vboxSidebarMain.setAlignment(Pos.CENTER);
        vboxSidebarMain.setPrefWidth(110);
        vboxSidebarMain.setBackground(new Background(new BackgroundFill(Color.rgb(200, 200, 200, 0.6), new CornerRadii(20), Insets.EMPTY)));
        vboxSidebarMain.setVisible(false);

        initIconSpaces(vboxSidebarMain);
        initIconsCalendars(vboxSidebarMain);
        initIconTasks(vboxSidebarMain);
        initIconButtonTimer(vboxSidebarMain);
        initIconSounds(vboxSidebarMain);
        initIconButtonNotes(vboxSidebarMain);
        initIconsBreathes(vboxSidebarMain);

        VBox vBoxSidebar = effectSidebar(vboxSidebarMain);
        hbox.getChildren().addAll(vBoxSidebar);
        hbox.setPadding(new Insets(30));
    }

    public void mouseClickEffect(Button button, String type) {
        button.setOnAction(event -> {
            switch (type) {
                case "Timer":
                    if (stageTimer != null && stageTimer.isShowing()) {
                        stageTimer.close();
                        stageTimer = null;
                        updateButtonState(button, true);
                    } else {
                        stageTimer = new Stage();
                        try {
                            new Timer().start(stageTimer);
                            stageTimer.setOnCloseRequest(e -> {
                                stageTimer = null;
                                updateButtonState(button, true);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButtonState(button, false);
                    }
                    break;
                case "Breathes":
                    if (stageBreathe != null && stageBreathe.isShowing()) {
                        stageBreathe.close();
                        stageBreathe = null;
                        updateButtonState(button, true);
                    } else {
                        stageBreathe = new Stage();
                        try {
                            new Breathe().start(stageBreathe);
                            stageBreathe.setOnCloseRequest(e -> {
                                stageBreathe = null;
                                updateButtonState(button, true);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButtonState(button, false);
                    }
                    break;
                case "Sounds":
                    if (stageSound != null && stageSound.isShowing()) {
                        stageSound.close();
                        if (soundBoard != null && soundBoard.mediaPlayer != null) {
                            soundBoard.mediaPlayer.stop();
                            soundBoard.mediaPlayer.dispose();
                        }
                        stageSound = null;
                        updateButtonState(button, true);
                    } else {
                        stageSound = new Stage();
                        try {
                            soundBoard = new SoundBoard();
                            soundBoard.start(stageSound);
                            stageSound.setOnCloseRequest(e -> {
                                if (soundBoard != null && soundBoard.mediaPlayer != null) {
                                    soundBoard.mediaPlayer.stop();
                                    soundBoard.mediaPlayer.dispose();
                                }
                                stageSound = null;
                                updateButtonState(button, true);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButtonState(button, false);
                    }
                    break;
                case "Notes":
                    if (stageNotes != null && stageNotes.isShowing()) {
                        stageNotes.close();
                        stageNotes = null;
                        updateButtonState(button, true);
                    } else {
                        stageNotes = new Stage();
                        try {
                            new Notes().start(stageNotes);
                            stageNotes.setOnCloseRequest(e -> {
                                stageNotes = null;
                                updateButtonState(button, true);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButtonState(button, false);
                    }
                    break;
                case "Spaces":
                    if (stageSpace != null && stageSpace.isShowing()) {
                        stageSpace.close();
                        stageSpace = null;
                        updateButtonState(button, true);
                    } else {
                        stageSpace = new Stage();
                        try {
                            new SpaceApp().start(stageSpace);
                            stageSpace.setOnCloseRequest(e -> {
                                stageSpace = null;
                                updateButtonState(button, true);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButtonState(button, false);
                    }
                    break;
                case "Tasks":
                    if (stageTasks != null && stageTasks.isShowing()) {
                        stageTasks.close();
                        stageTasks = null;
                        updateButtonState(button, true);
                    } else {
                        stageTasks = new Stage();
                        try {
                            new Tasks().start(stageTasks);
                            stageTasks.setOnCloseRequest(e -> {
                                stageTasks = null;
                                updateButtonState(button, true);
                            });
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButtonState(button, false);
                    }
                    break;

                case "Calendars":
                    if (stagecal != null && stagecal.isShowing()) {
                        stagecal.close();
                        stagecal = null;
                        updateButtonState(button, true);
                    } else {
                        stagecal = new Stage();
                        try {
                            new CalendarApp().start(stagecal);
                            stagecal.setOnCloseRequest(e -> {
                                stagecal = null;
                                updateButtonState(button, true);
                            });
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButtonState(button, false);
                    }
            }
        });

        button.setOnMousePressed(event -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(0.8);
            scaleDown.setToY(0.8);
            scaleDown.play();
        });

        button.setOnMouseReleased(event -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1);
            scaleUp.setToY(1);
            scaleUp.play();
        });

        button.setOnMouseEntered(event -> {
            boolean isOpened = switch (type) {
                case "Timer" -> stageTimer != null && stageTimer.isShowing();
                case "Breathes" -> stageBreathe != null && stageBreathe.isShowing();
                case "Sounds" -> stageSound != null && stageSound.isShowing();
                case "Notes" -> stageNotes != null && stageNotes.isShowing();
                case "Spaces" -> stageSpace != null && stageSpace.isShowing();
                case "Calendars" -> stagecal != null && stagecal.isShowing();
                case "Tasks" -> stageTasks != null && stageTasks.isShowing();
                default -> false;
            };
            if (!isOpened) {
                button.setBackground(new Background(new BackgroundFill(Color.rgb(239, 239, 239), new CornerRadii(15), Insets.EMPTY)));
                button.setTextFill(Color.INDIANRED);
            }
        });

        button.setOnMouseExited(event -> {
            boolean isOpened = switch (type) {
                case "Timer" -> stageTimer != null && stageTimer.isShowing();
                case "Breathes" -> stageBreathe != null && stageBreathe.isShowing();
                case "Sounds" -> stageSound != null && stageSound.isShowing();
                case "Notes" -> stageNotes != null && stageNotes.isShowing();
                case "Spaces" -> stageSpace != null && stageSpace.isShowing();
                case "Calendars" -> stagecal != null && stagecal.isShowing();
                case "Tasks" -> stageTasks != null && stageTasks.isShowing();
                default -> false;
            };
            if (!isOpened) {
                button.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                button.setTextFill(Color.rgb(85, 85, 85));
            }
        });
    }

    private void updateButtonState(Button button, boolean isOpened) {
        if (isOpened) {
            button.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            button.setTextFill(Color.rgb(85, 85, 85));
            button.setOpacity(1.0);
        } else {
            button.setBackground(new Background(new BackgroundFill(Color.rgb(239, 239, 239), new CornerRadii(15), Insets.EMPTY)));
            button.setTextFill(Color.INDIANRED);
            button.setOpacity(0.7);
        }
    }

    private void initIconButtonTimer(VBox vbox) {
        iconTimer = new ImageView(new Image(getClass().getResourceAsStream("/IconsTimer.png")));
        iconTimer.setFitHeight(30);
        iconTimer.setFitWidth(30);
        btnTimer = new Button("Timer", iconTimer);
        btnTimer.setTextFill(Color.rgb(85, 85, 85));
        btnTimer.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        btnTimer.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        btnTimer.setContentDisplay(ContentDisplay.TOP);
        mouseClickEffect(btnTimer, "Timer");
        vbox.getChildren().add(btnTimer);
    }

    private void initIconButtonNotes(VBox vbox) {
        iconNotes = new ImageView(new Image(getClass().getResourceAsStream("/IconsNotes.png")));
        iconNotes.setFitHeight(30);
        iconNotes.setFitWidth(30);
        btnNotes = new Button("Notes", iconNotes);
        btnNotes.setTextFill(Color.rgb(85, 85, 85));
        btnNotes.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        btnNotes.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        btnNotes.setContentDisplay(ContentDisplay.TOP);
        mouseClickEffect(btnNotes, "Notes");
        vbox.getChildren().add(btnNotes);
    }

    private void initIconSpaces(VBox vbox) {
        iconSpaces = new ImageView(new Image(getClass().getResourceAsStream("/IconsSpaces.png")));
        iconSpaces.setFitHeight(30);
        iconSpaces.setFitWidth(30);
        btnSpaces = new Button("Spaces", iconSpaces);
        btnSpaces.setTextFill(Color.rgb(85, 85, 85));
        btnSpaces.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        btnSpaces.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        btnSpaces.setContentDisplay(ContentDisplay.TOP);
        mouseClickEffect(btnSpaces, "Spaces");
        vbox.getChildren().add(btnSpaces);
    }

    private void initIconTasks(VBox vbox) {
        iconTasks = new ImageView(new Image(getClass().getResourceAsStream("/IconsTasks.png")));
        iconTasks.setFitHeight(30);
        iconTasks.setFitWidth(30);
        btnTasks = new Button("Tasks", iconTasks);
        btnTasks.setTextFill(Color.rgb(85, 85, 85));
        btnTasks.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        btnTasks.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        btnTasks.setContentDisplay(ContentDisplay.TOP);
        mouseClickEffect(btnTasks, "Tasks");
        vbox.getChildren().add(btnTasks);
    }

    private void initIconSounds(VBox vbox) {
        iconSounds = new ImageView(new Image(getClass().getResourceAsStream("/IconsSound.png")));
        iconSounds.setFitWidth(30);
        iconSounds.setFitHeight(30);
        btnSound = new Button("Sounds", iconSounds);
        btnSound.setTextFill(Color.rgb(85, 85, 85));
        btnSound.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        btnSound.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        btnSound.setContentDisplay(ContentDisplay.TOP);
        mouseClickEffect(btnSound, "Sounds");
        vbox.getChildren().add(btnSound);
    }

    private void initIconsBreathes(VBox vbox) {
        iconBreathe = new ImageView(new Image(getClass().getResourceAsStream("/IconsBreathe.png")));
        iconBreathe.setFitHeight(30);
        iconBreathe.setFitWidth(30);
        btnBreathe = new Button("Breathes", iconBreathe);
        btnBreathe.setTextFill(Color.rgb(85, 85, 85));
        btnBreathe.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        btnBreathe.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        btnBreathe.setContentDisplay(ContentDisplay.TOP);
        mouseClickEffect(btnBreathe, "Breathes");
        vbox.getChildren().add(btnBreathe);
    }

    private void initIconsCalendars(VBox vbox) {
        iconCal = new ImageView(new Image(getClass().getResourceAsStream("/IconsCal.png")));
        iconCal.setFitHeight(30);
        iconCal.setFitWidth(30);
        btnCal = new Button("Calendars", iconCal);
        btnCal.setTextFill(Color.rgb(85, 85, 85));
        btnCal.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        btnCal.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        btnCal.setContentDisplay(ContentDisplay.TOP);
        mouseClickEffect(btnCal, "Calendars");
        vbox.getChildren().add(btnCal);
    }

    public static void main(String[] args) {
        launch(args);
    }
}