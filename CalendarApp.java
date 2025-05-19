package Calendar;

import LinkTasksAndCal.LinkTasksAndCalModel;
import Tasks.Tasks;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CalendarApp extends Application {

    private LocalDate currentDate = LocalDate.now(ZoneId.of("GMT+7"));
    private final Text dateText = new Text();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
    private static Stage calendarStage;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yy", new java.util.Locale("vi", "VN"));

    static {
        System.setProperty("user.timezone", "GMT+7");
    }

    public CalendarApp() {
    }

    @Override
    public void start(Stage primaryStage) {
        calendarStage = primaryStage;
        primaryStage.setTitle("Calendar Application");

        System.out.println("JVM default time zone: " + ZoneId.systemDefault());
        System.out.println("Current time (system): " + LocalDateTime.now());
        System.out.println("Current time (GMT+7): " + LocalDateTime.now(ZoneId.of("GMT+7")));

        BorderPane root = new BorderPane();
        root.setTop(createTopPanel());
        root.setLeft(createCalendarSidebar());
        root.setCenter(createDailySchedulePane());
        root.setBackground(new Background(new BackgroundFill(Color.web("#E0ECEF"), null, null)));

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(e -> calendarStage = null);
        primaryStage.show();

        initIcon(primaryStage);

        updateDateText();
    }

    private void initIcon(Stage primaryStage) {
        Image image = new Image(getClass().getResourceAsStream("/IconsCal.png"));
        primaryStage.getIcons().add(image);
    }


    private HBox createTopPanel() {
        Button prevDayButton = new Button("<");
        styleButton(prevDayButton, "#4C7C8A", "#FFFFFF", 8);
        prevDayButton.setOnAction(e -> updateDate(-1));

        Button nextDayButton = new Button(">");
        styleButton(nextDayButton, "#4C7C8A", "#FFFFFF", 8);
        nextDayButton.setOnAction(e -> updateDate(1));

        Button todayButton = new Button("Today");
        styleButton(todayButton, "#4C7C8A", "#FFFFFF", 8);
        todayButton.setOnAction(e -> {
            currentDate = LocalDate.now(ZoneId.of("GMT+7"));
            updateDateText();
        });

        Button tasksButton = new Button("View Tasks");
        styleButton(tasksButton, "#4C7C8A", "#FFFFFF", 8);
        tasksButton.setOnAction(e -> {
            if (Tasks.getMainStage() == null) {
                Tasks mainApp = new Tasks();
                Stage mainStage = new Stage();
                mainApp.start(mainStage);
            } else {
                Tasks.getMainStage().toFront();
            }
        });

        dateText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        dateText.setFill(Color.WHITE);

        HBox topPanel = new HBox(20, prevDayButton, dateText, nextDayButton, todayButton, tasksButton);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(15));
        topPanel.setBackground(new Background(new BackgroundFill(Color.web("#4C7C8A"), null, null)));
        topPanel.setEffect(new DropShadow(10, 0, 2, Color.gray(0.1)));

        return topPanel;
    }

    private VBox createCalendarSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(300);
        sidebar.setBackground(new Background(new BackgroundFill(Color.web("#F5F8FA"), null, null)));
        sidebar.setBorder(new Border(new BorderStroke(Color.web("#E0ECEF"), BorderStrokeStyle.SOLID, null, new BorderWidths(0, 1, 0, 0))));
        sidebar.setEffect(new DropShadow(10, 0, 2, Color.gray(0.1)));

        Label monthLabel = new Label(currentDate.getMonth().toString() + " " + currentDate.getYear());
        monthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        monthLabel.setTextFill(Color.web("#2D3E50"));
        monthLabel.setPadding(new Insets(10, 0, 10, 0));

        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Text day = new Text(days[i]);
            day.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            day.setFill(Color.web("#2D3E50"));
            GridPane.setHalignment(day, HPos.CENTER);
            calendarGrid.add(day, i, 0);
        }

        updateCalendarGrid(calendarGrid);

        Button prevMonthButton = new Button("Previous Month");
        styleButton(prevMonthButton, "#4C7C8A", "#FFFFFF", 8);
        prevMonthButton.setOnAction(e -> {
            currentDate = currentDate.minusMonths(1);
            monthLabel.setText(currentDate.getMonth().toString() + " " + currentDate.getYear());
            updateCalendarGrid(calendarGrid);
        });

        Button nextMonthButton = new Button("Next Month");
        styleButton(nextMonthButton, "#4C7C8A", "#FFFFFF", 8);
        nextMonthButton.setOnAction(e -> {
            currentDate = currentDate.plusMonths(1);
            monthLabel.setText(currentDate.getMonth().toString() + " " + currentDate.getYear());
            updateCalendarGrid(calendarGrid);
        });

        HBox monthNavigation = new HBox(10, prevMonthButton, nextMonthButton);
        monthNavigation.setAlignment(Pos.CENTER);

        sidebar.getChildren().addAll(monthLabel, monthNavigation, calendarGrid);
        return sidebar;
    }

    private void updateCalendarGrid(GridPane calendarGrid) {
        calendarGrid.getChildren().clear();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Text day = new Text(days[i]);
            day.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            day.setFill(Color.web("#2D3E50"));
            GridPane.setHalignment(day, HPos.CENTER);
            calendarGrid.add(day, i, 0);
        }

        LocalDate firstOfMonth = currentDate.withDayOfMonth(1);
        int startDay = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 = Monday, 6 = Sunday
        int daysInMonth = firstOfMonth.lengthOfMonth();

        LocalDate prevMonth = firstOfMonth.minusMonths(1);
        int daysInPrevMonth = prevMonth.lengthOfMonth();
        int prevMonthStartDay = daysInPrevMonth - startDay + 1;

        int row = 1;
        int col = 0;
        for (int day = prevMonthStartDay; day <= daysInPrevMonth; day++) {
            StackPane dayPane = createDayPane(day, prevMonth.withDayOfMonth(day), false);
            GridPane.setHalignment(dayPane, HPos.CENTER);
            calendarGrid.add(dayPane, col, row);
            col++;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (col > 6) {
                col = 0;
                row++;
            }
            StackPane dayPane = createDayPane(day, firstOfMonth.withDayOfMonth(day), true);
            GridPane.setHalignment(dayPane, HPos.CENTER);
            calendarGrid.add(dayPane, col, row);
            col++;
        }

        LocalDate nextMonth = firstOfMonth.plusMonths(1);
        int nextMonthDay = 1;
        while (row < 6 || col <= 6) {
            if (col > 6) {
                col = 0;
                row++;
            }
            if (row >= 6) break;

            StackPane dayPane = createDayPane(nextMonthDay, nextMonth.withDayOfMonth(nextMonthDay), false);
            GridPane.setHalignment(dayPane, HPos.CENTER);
            calendarGrid.add(dayPane, col, row);
            col++;
            nextMonthDay++;
        }
    }

    private StackPane createDayPane(int day, LocalDate date, boolean isCurrentMonth) {
        StackPane dayPane = new StackPane();
        Button dayButton = new Button(String.valueOf(day));
        dayButton.setFont(Font.font("Arial", 14));
        dayButton.setTextFill(isCurrentMonth ? Color.web("#2D3E50") : Color.web("#A9B5BB"));
        dayButton.setBackground(new Background(new BackgroundFill(Color.web("#F5F8FA"), new CornerRadii(50), null)));
        dayButton.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, new CornerRadii(50), new BorderWidths(2))));
        dayButton.setPrefSize(40, 40);
        dayButton.setAlignment(Pos.CENTER);

        long taskCount = Tasks.sharedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getStartTime().toLocalDate().equals(date))
                .count();
        if (taskCount > 0) {
            Label badge = new Label(String.valueOf(taskCount));
            badge.setStyle("-fx-background-color: #FFCA28; -fx-background-radius: 10; -fx-padding: 2 4; -fx-font-size: 10; -fx-text-fill: #2D3E50;");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            dayPane.getChildren().addAll(dayButton, badge);
        } else {
            dayPane.getChildren().add(dayButton);
        }

        String taskTitles = Tasks.sharedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getStartTime().toLocalDate().equals(date))
                .map(LinkTasksAndCalModel::getTitle)
                .collect(Collectors.joining("\n"));
        if (!taskTitles.isEmpty()) {
            Tooltip tooltip = new Tooltip(taskTitles);
            tooltip.setFont(Font.font("Arial", 12));
            Tooltip.install(dayPane, tooltip);
        }

        if (date.equals(LocalDate.now(ZoneId.of("GMT+7")))) {
            dayButton.setBackground(new Background(new BackgroundFill(Color.web("#A3BFFA"), new CornerRadii(50), null)));
            dayButton.setTextFill(Color.WHITE);
        }
        if (date.equals(currentDate)) {
            dayButton.setBackground(new Background(new BackgroundFill(Color.web("#4C7C8A"), new CornerRadii(50), null)));
            dayButton.setTextFill(Color.WHITE);
            dayButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        }

        dayButton.setOnAction(e -> {
            currentDate = date;
            updateDateText();
            BorderPane root = (BorderPane) dateText.getScene().getRoot();
            ScrollPane scrollPane = (ScrollPane) root.getCenter();
            StackPane contentPane = (StackPane) scrollPane.getContent();
            VBox hoursBox = (VBox) contentPane.getChildren().get(1);
            updateEventsDisplay(hoursBox);
        });

        dayButton.setOnMouseEntered(e -> {
            if (!dayButton.getBackground().getFills().get(0).getFill().equals(Color.web("#4C7C8A")) &&
                    !dayButton.getBackground().getFills().get(0).getFill().equals(Color.web("#A3BFFA"))) {
                dayButton.setBackground(new Background(new BackgroundFill(Color.web("#E0ECEF"), new CornerRadii(50), null)));
                dayButton.setBorder(new Border(new BorderStroke(Color.web("#4C7C8A"), BorderStrokeStyle.SOLID, new CornerRadii(50), new BorderWidths(2))));
            }
        });
        dayButton.setOnMouseExited(e -> {
            if (!dayButton.getBackground().getFills().get(0).getFill().equals(Color.web("#4C7C8A")) &&
                    !dayButton.getBackground().getFills().get(0).getFill().equals(Color.web("#A3BFFA"))) {
                dayButton.setBackground(new Background(new BackgroundFill(Color.web("#F5F8FA"), new CornerRadii(50), null)));
                dayButton.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, new CornerRadii(50), new BorderWidths(2))));
            }
        });

        return dayPane;
    }

    private ScrollPane createDailySchedulePane() {
        VBox hoursBox = new VBox();
        hoursBox.setAlignment(Pos.TOP_LEFT);
        hoursBox.setBackground(new Background(new BackgroundFill(Color.web("#F5F8FA"), null, null)));
        hoursBox.setMinWidth(700);

        HBox allDayRow = new HBox();
        allDayRow.setPrefHeight(40);
        allDayRow.setSpacing(5);

        Text allDayText = new Text("All Day");
        allDayText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        allDayText.setFill(Color.web("#2D3E50"));
        allDayText.setWrappingWidth(50);

        VBox allDayEventsBox = new VBox(1);
        allDayEventsBox.setMinWidth(500);

        allDayRow.getChildren().addAll(allDayText, allDayEventsBox);
        allDayRow.setAlignment(Pos.CENTER_LEFT);
        hoursBox.getChildren().add(allDayRow);

        IntStream.range(0, 24).forEach(hour -> {
            HBox hourRow = new HBox();
            hourRow.setPrefHeight(40);
            hourRow.setSpacing(5);

            Text hourText = new Text(String.format("%02d:00", hour));
            hourText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            hourText.setFill(Color.web("#2D3E50"));
            hourText.setWrappingWidth(50);

            VBox eventsBox = new VBox(1);
            eventsBox.setMinWidth(650);
            eventsBox.setOnDragOver(event -> {
                if (event.getGestureSource() != eventsBox && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            eventsBox.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String taskId = db.getString();
                    LinkTasksAndCalModel taskToMove = Tasks.sharedTasks.stream()
                            .filter(t -> t.getId().equals(taskId))
                            .findFirst()
                            .orElse(null);
                    if (taskToMove != null) {
                        int newHour = hoursBox.getChildren().indexOf(hourRow) - 1;
                        LocalDateTime newStartTime = LocalDateTime.of(
                                taskToMove.getStartTime().toLocalDate(),
                                LocalTime.of(newHour, taskToMove.getStartTime().toLocalTime().getMinute())
                        );
                        taskToMove.setStartTime(newStartTime);
                        if (taskToMove.getEndTime() != null && taskToMove.getEndTime().isBefore(newStartTime)) {
                            taskToMove.setEndTime(newStartTime.plusHours(1));
                        }
                        success = true;
                        updateEventsDisplay(hoursBox);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            hourRow.getChildren().addAll(hourText, eventsBox);
            hourRow.setAlignment(Pos.CENTER_LEFT);
            hoursBox.getChildren().add(hourRow);
        });

        Line currentTimeLine = new Line(0, 0, 700, 0);
        currentTimeLine.setStroke(Color.RED);
        currentTimeLine.setStrokeWidth(2);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                LocalTime currentTime = LocalTime.now(ZoneId.of("GMT+7"));
                double rowHeight = 40;
                double yPosition = rowHeight * currentTime.getHour() + (rowHeight * currentTime.getMinute() / 60.0);
                yPosition += 40; // Shift down by "All Day" row
                currentTimeLine.setTranslateY(yPosition);
            }
        };
        timer.start();

        StackPane contentPane = new StackPane();
        contentPane.getChildren().addAll(currentTimeLine, hoursBox);
        contentPane.setAlignment(Pos.TOP_LEFT);
        contentPane.setMinWidth(700);

        ScrollPane scrollPane = new ScrollPane(contentPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        scrollPane.setMinWidth(700);

        updateEventsDisplay(hoursBox);

        Tasks.sharedTasks.addListener((javafx.collections.ListChangeListener<LinkTasksAndCalModel>) c -> updateEventsDisplay(hoursBox));

        return scrollPane;
    }

    private void updateEventsDisplay(VBox hoursBox) {
        for (int i = 0; i < hoursBox.getChildren().size(); i++) {
            HBox row = (HBox) hoursBox.getChildren().get(i);
            VBox eventsBox = (VBox) row.getChildren().get(1);
            eventsBox.getChildren().clear();
        }

        for (LinkTasksAndCalModel linkTasksAndCal : Tasks.sharedTasks) {
            if (linkTasksAndCal == null || linkTasksAndCal.getStartTime() == null || !linkTasksAndCal.getStartTime().toLocalDate().equals(currentDate)) {
                continue;
            }

            VBox targetEventsBox;
            if (linkTasksAndCal.getStartTime().toLocalTime().equals(LocalTime.MIDNIGHT) || linkTasksAndCal.getStartTime().toLocalTime().equals(LocalTime.of(0, 0))) {
                HBox allDayRow = (HBox) hoursBox.getChildren().get(0);
                targetEventsBox = (VBox) allDayRow.getChildren().get(1);
            } else {
                int hour = linkTasksAndCal.getStartTime().toLocalTime().getHour();
                HBox hourRow = (HBox) hoursBox.getChildren().get(hour + 1);
                targetEventsBox = (VBox) hourRow.getChildren().get(1);
            }

            HBox compactView = createCompactTaskView(linkTasksAndCal, targetEventsBox);
            targetEventsBox.getChildren().add(compactView);
        }
    }

    private HBox createCompactTaskView(LinkTasksAndCalModel linkTasksAndCal, VBox targetEventsBox) {
        HBox compactView = new HBox(3);
        compactView.setPadding(new Insets(1));
        compactView.setBackground(new Background(new BackgroundFill(Color.web("#F5F8FA"), new CornerRadii(4), null)));
        compactView.setBorder(new Border(new BorderStroke(Color.web("#E0ECEF"), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        compactView.setEffect(new DropShadow(2, 0, 1, Color.gray(0.1)));

        Label titleLabel = new Label(linkTasksAndCal.getTitle() + " [" + linkTasksAndCal.getPriority() + "]");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        titleLabel.setTextFill(Color.web("#2D3E50"));
        titleLabel.setWrapText(true);
        titleLabel.setPrefWidth(400);
        titleLabel.setMaxWidth(400);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label timeLabel = new Label();
        String timeText = linkTasksAndCal.getStartTime() != null ? timeFormatter.format(linkTasksAndCal.getStartTime()) : "";
        timeLabel.setText(timeText);
        timeLabel.setFont(Font.font("Arial", 8));
        timeLabel.setTextFill(Color.web("#2D3E50"));
        timeLabel.setWrapText(true);
        timeLabel.setPrefWidth(150);
        timeLabel.setMaxWidth(150);
        HBox.setHgrow(timeLabel, Priority.NEVER);

        compactView.getChildren().addAll(titleLabel, timeLabel);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem moveToTaskListItem = new MenuItem("Move to Task List");

        editItem.setOnAction(e -> {
            if (Tasks.getMainStage() == null) {
                Tasks mainApp = new Tasks();
                Stage mainStage = new Stage();
                mainApp.start(mainStage);
            }
            new Tasks().showAddEditTaskDialog(linkTasksAndCal);
        });

        deleteItem.setOnAction(e -> {
            Tasks.sharedTasks.remove(linkTasksAndCal);
        });

        moveToTaskListItem.setOnAction(e -> {
            linkTasksAndCal.setStartTime(null);
            linkTasksAndCal.setEndTime(null);
            updateEventsDisplay((VBox) ((HBox) targetEventsBox.getParent()).getParent());
        });

        contextMenu.getItems().addAll(editItem, deleteItem, moveToTaskListItem);

        compactView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                int index = targetEventsBox.getChildren().indexOf(compactView);
                targetEventsBox.getChildren().set(index, createDetailedTaskView(linkTasksAndCal, targetEventsBox));
            } else if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(compactView, e.getScreenX(), e.getScreenY());
            }
        });

        compactView.setOnDragDetected(event -> {
            Dragboard db = compactView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(linkTasksAndCal.getId()));
            db.setContent(content);
            event.consume();
        });

        compactView.setOnMouseEntered(e -> {
            compactView.setEffect(new DropShadow(4, 0, 2, Color.gray(0.2)));
            compactView.setScaleX(1.01);
            compactView.setScaleY(1.01);
        });
        compactView.setOnMouseExited(e -> {
            compactView.setEffect(new DropShadow(2, 0, 1, Color.gray(0.1)));
            compactView.setScaleX(1.0);
            compactView.setScaleY(1.0);
        });

        return compactView;
    }

    private VBox createDetailedTaskView(LinkTasksAndCalModel linkTasksAndCalsk, VBox targetEventsBox) {
        VBox detailedView = new VBox(1);
        detailedView.setPadding(new Insets(2));
        detailedView.setBackground(new Background(new BackgroundFill(Color.web("#F5F8FA"), new CornerRadii(4), null)));
        detailedView.setBorder(new Border(new BorderStroke(Color.web("#E0ECEF"), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        detailedView.setEffect(new DropShadow(2, 0, 1, Color.gray(0.1)));

        Label titleLabel = new Label(linkTasksAndCalsk.getTitle() + " [" + linkTasksAndCalsk.getPriority() + "]");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        titleLabel.setTextFill(Color.web("#2D3E50"));
        titleLabel.setWrapText(true);
        titleLabel.setPrefWidth(400);
        titleLabel.setMaxWidth(400);

        Label timeLabel = new Label();
        String timeText = "";
        if (linkTasksAndCalsk.getStartTime() != null && linkTasksAndCalsk.getEndTime() != null) {
            timeText = timeFormatter.format(linkTasksAndCalsk.getStartTime()) + " - " + timeFormatter.format(linkTasksAndCalsk.getEndTime());
            timeLabel.setTextFill(LocalDateTime.now().isAfter(linkTasksAndCalsk.getEndTime()) && !linkTasksAndCalsk.isCompleted() ? Color.RED : Color.web("#2D3E50"));
        } else if (linkTasksAndCalsk.getEndTime() != null) {
            timeText = "Hết hạn: " + timeFormatter.format(linkTasksAndCalsk.getEndTime());
            timeLabel.setTextFill(LocalDateTime.now().isAfter(linkTasksAndCalsk.getEndTime()) && !linkTasksAndCalsk.isCompleted() ? Color.RED : Color.web("#2D3E50"));
        }
        timeLabel.setText(timeText);
        timeLabel.setFont(Font.font("Arial", 8));
        timeLabel.setTextFill(Color.web("#2D3E50"));
        timeLabel.setWrapText(true);
        timeLabel.setPrefWidth(150);
        timeLabel.setMaxWidth(150);

        Label descriptionLabel = new Label(linkTasksAndCalsk.getDescription());
        descriptionLabel.setFont(Font.font("Arial", 8));
        descriptionLabel.setTextFill(Color.web("#2D3E50"));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefWidth(400);
        descriptionLabel.setMaxWidth(400);

        detailedView.getChildren().addAll(titleLabel, timeLabel, descriptionLabel);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem moveToTaskListItem = new MenuItem("Move to Task List");

        editItem.setOnAction(e -> {
            if (Tasks.getMainStage() == null) {
                Tasks mainApp = new Tasks();
                Stage mainStage = new Stage();
                mainApp.start(mainStage);
            }
            new Tasks().showAddEditTaskDialog(linkTasksAndCalsk);
        });

        deleteItem.setOnAction(e -> {
            Tasks.sharedTasks.remove(linkTasksAndCalsk);
        });

        moveToTaskListItem.setOnAction(e -> {
            linkTasksAndCalsk.setStartTime(null);
            linkTasksAndCalsk.setEndTime(null);
            updateEventsDisplay((VBox) ((HBox) targetEventsBox.getParent()).getParent());
        });

        contextMenu.getItems().addAll(editItem, deleteItem, moveToTaskListItem);

        detailedView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                int index = targetEventsBox.getChildren().indexOf(detailedView);
                targetEventsBox.getChildren().set(index, createCompactTaskView(linkTasksAndCalsk, targetEventsBox));
            } else if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(detailedView, e.getScreenX(), e.getScreenY());
            }
        });

        detailedView.setOnDragDetected(event -> {
            Dragboard db = detailedView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(linkTasksAndCalsk.getId()));
            db.setContent(content);
            event.consume();
        });

        detailedView.setOnMouseEntered(e -> {
            detailedView.setEffect(new DropShadow(4, 0, 2, Color.gray(0.2)));
            detailedView.setScaleX(1.01);
            detailedView.setScaleY(1.01);
        });
        detailedView.setOnMouseExited(e -> {
            detailedView.setEffect(new DropShadow(2, 0, 1, Color.gray(0.1)));
            detailedView.setScaleX(1.0);
            detailedView.setScaleY(1.0);
        });

        return detailedView;
    }

    private void updateDate(int days) {
        currentDate = currentDate.plusDays(days);
        updateDateText();
        BorderPane root = (BorderPane) dateText.getScene().getRoot();
        ScrollPane scrollPane = (ScrollPane) root.getCenter();
        StackPane contentPane = (StackPane) scrollPane.getContent();
        VBox hoursBox = (VBox) contentPane.getChildren().get(1);
        updateEventsDisplay(hoursBox);
    }

    private void updateDateText() {
        dateText.setText(currentDate.format(dateFormatter));
    }

    public void setCurrentDate(LocalDate date) {
        if (date != null) {
            this.currentDate = date;
            updateDateText();
            BorderPane root = (BorderPane) dateText.getScene().getRoot();
            if (root != null) {
                ScrollPane scrollPane = (ScrollPane) root.getCenter();
                if (scrollPane != null) {
                    StackPane contentPane = (StackPane) scrollPane.getContent();
                    if (contentPane != null) {
                        VBox hoursBox = (VBox) contentPane.getChildren().get(1);
                        updateEventsDisplay(hoursBox);
                    }
                }
            }
        }
    }

    private void styleButton(Button button, String bgColor, String textColor, double radius) {
        button.setFont(Font.font("Arial", 14));
        button.setBackground(new Background(new BackgroundFill(Color.web(bgColor), new CornerRadii(radius), null)));
        button.setTextFill(Color.web(textColor));
        button.setPadding(new Insets(8, 16, 8, 16));
        button.setEffect(new DropShadow(5, 0, 2, Color.gray(0.2)));

        button.setOnMouseEntered(e -> {
            button.setBackground(new Background(new BackgroundFill(Color.web("#355B66"), new CornerRadii(radius), null)));
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        button.setOnMouseExited(e -> {
            button.setBackground(new Background(new BackgroundFill(Color.web(bgColor), new CornerRadii(radius), null)));
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
    }

    public static Stage getCalendarStage() {
        return calendarStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}