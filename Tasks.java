package Tasks;
import Calendar.CalendarApp;
import DAO.DAOTasks;
import LinkTasksAndCal.LinkTasksAndCalModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tasks extends Application {

    public static ObservableList<LinkTasksAndCalModel> sharedTasks = FXCollections.observableArrayList();
    private static Stage mainStage;
    private ListView<LinkTasksAndCalModel> taskListView = new ListView<>(sharedTasks);
    private FilteredList<LinkTasksAndCalModel> filteredTasks; // Thêm trường này
    private SortedList<LinkTasksAndCalModel> sortedTasks; // Thêm trường này
    private ProgressBar completionProgressBar = new ProgressBar(0);
    private Label completionLabel = new Label("0/0");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", new java.util.Locale("vi", "VN"));
    private CalendarApp calendarApp;
    private VBox notificationArea;
    public  ComboBox<String> sortCombo; // Thêm trường này


    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        primaryStage.setTitle("Study Task Manager");

        // Tải dữ liệu từ cơ sở dữ liệu
        sharedTasks.clear();
        sharedTasks.addAll(DAOTasks.getInstance().selectAllTasks());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));
        mainLayout.setBackground(new Background(new BackgroundFill(Color.SEASHELL, null, null)));

        mainLayout.setTop(createFilterBar());
        VBox centerLayout = new VBox(10);
        centerLayout.setPadding(new Insets(10));
        notificationArea = createNotificationArea();

        // Thiết lập FilteredList và SortedList
        filteredTasks = new FilteredList<>(sharedTasks, p -> true);
        sortedTasks = new SortedList<>(filteredTasks);
        taskListView.setItems(sortedTasks);

        // Add search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setStyle("-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 8;");

// Trong phương thức start
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredTasks.setPredicate(task -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    return true; // Hiển thị tất cả khi không có từ khóa
                }
                // Chuẩn hóa chuỗi tìm kiếm và loại bỏ dấu
                String searchText = Normalizer.normalize(newVal.trim(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase();
                String title = task.getTitle() != null
                        ? Normalizer.normalize(task.getTitle(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase()
                        : "";
                String description = task.getDescription() != null
                        ? Normalizer.normalize(task.getDescription(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase()
                        : "";
                return title.contains(searchText) || description.contains(searchText);
            });
            if (sortCombo != null) {
                sortTasks(sortCombo.getValue());
            }
        });

        centerLayout.getChildren().addAll(searchField, notificationArea, taskListView, createProgressBarAndLabel());
        mainLayout.setCenter(centerLayout);

        taskListView.setCellFactory(param -> new TaskListCell());
        updateCompletionProgress();
        setupNotificationCheck();

// Thêm listener sau khi sortCombo được khởi tạo
        sharedTasks.addListener((ListChangeListener<LinkTasksAndCalModel>) c -> {
            Platform.runLater(() -> {
                updateCompletionProgress();
                checkOverdueTasks();
                // Đảm bảo sortTasks được gọi, sử dụng giá trị mặc định nếu sortCombo là null
                String sortValue = (sortCombo != null && sortCombo.getValue() != null) ? sortCombo.getValue() : "Title (A-Z)";
                sortTasks(sortValue);
                if (calendarApp != null) {
                    calendarApp.updateTasks(sharedTasks); // Đồng bộ với CalendarApp
                }
            });
        });

        taskListView.setOnDragDetected(event -> {
            if (taskListView.getSelectionModel().getSelectedItem() == null) return;
            Dragboard db = taskListView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            LinkTasksAndCalModel selectedTask = taskListView.getSelectionModel().getSelectedItem();
            content.putString(String.valueOf(selectedTask.getId())); // Sử dụng taskId
            db.setContent(content);
            event.consume();
        });

        taskListView.setOnDragOver(event -> {
            if (event.getGestureSource() != taskListView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        taskListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int draggedIndex = Integer.parseInt(db.getString());
                LinkTasksAndCalModel draggedTask = sharedTasks.get(draggedIndex);
                int dropIndex = taskListView.getItems().indexOf(event.getGestureTarget() instanceof TaskListCell ?
                        ((TaskListCell) event.getGestureTarget()).getItem() : draggedTask);
                if (dropIndex < 0 || dropIndex >= sharedTasks.size()) {
                    dropIndex = sharedTasks.size() - 1;
                }
                sharedTasks.remove(draggedIndex);
                sharedTasks.add(dropIndex, draggedTask);
                success = true;
                if (calendarApp != null) {
                    calendarApp.updateTasks(sharedTasks);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        taskListView.setOnDragDone(DragEvent::consume);

        Scene scene = new Scene(mainLayout, 600, 600);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.N && e.isControlDown()) {
                showAddEditTaskDialog(null);
            }
        });
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(e -> mainStage = null);

        initIcon(primaryStage);

        primaryStage.show();
    }

    private void initIcon(Stage stage){
        Image image = new Image(getClass().getResourceAsStream("/IconsTasks.png"));
        stage.getIcons().add(image);
    }

    private VBox createNotificationArea() {
        VBox notificationBox = new VBox(5);
        notificationBox.setPadding(new Insets(10));
        notificationBox.setBackground(new Background(new BackgroundFill(Color.SEASHELL, new CornerRadii(8), null)));
        notificationBox.setBorder(new Border(new BorderStroke(Color.web("#FFCA28"), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        notificationBox.setVisible(false);
        return notificationBox;
    }

    private void setupNotificationCheck() {
        checkOverdueTasks();
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> checkOverdueTasks()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void checkOverdueTasks() {
        notificationArea.getChildren().clear();
        boolean hasOverdue = false;
        for (LinkTasksAndCalModel linkTasksAndCal : sharedTasks) {
            if (linkTasksAndCal.getEndTime() != null && LocalDateTime.now().isAfter(linkTasksAndCal.getEndTime()) && !linkTasksAndCal.isCompleted()) {
                HBox notification = new HBox(10);
                Label message = new Label("Overdue: " + linkTasksAndCal.getTitle() + " (Due: " + dateTimeFormatter.format(linkTasksAndCal.getEndTime()) + ")");
                message.setFont(Font.font("Arial", 12));
                message.setTextFill(Color.web("#D32F2F"));
                Button actionButton = new Button("Edit");
                styleButton(actionButton, "#4C7C8A", "#FFFFFF", 4);
                actionButton.setOnAction(e -> showAddEditTaskDialog(linkTasksAndCal));
                notification.getChildren().addAll(message, actionButton);
                notificationArea.getChildren().add(notification);
                hasOverdue = true;
            }
        }
        notificationArea.setVisible(hasOverdue);
        if (hasOverdue && mainStage != null && !mainStage.isFocused()) {
            mainStage.requestFocus();
        }
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(15));
        filterBar.setBackground(new Background(new BackgroundFill(Color.web("#4C7C8A"), null, null)));
        filterBar.setEffect(new DropShadow(10, 0, 2, Color.gray(0.1)));

        Label titleLabel = new Label("Task Manager");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        sortCombo = new ComboBox<>(); // Gán giá trị cho trường
        sortCombo.getItems().addAll("Default", "By Title", "By Deadline", "By Priority");
        sortCombo.setValue("Default");
        styleComboBox(sortCombo);
        sortCombo.setOnAction(e -> sortTasks(sortCombo.getValue()));

        Button calendarButton = new Button("View Calendar");
        styleButton(calendarButton, "#4C7C8A", "#FFFFFF", 8);
        calendarButton.setOnAction(e -> showCalendar());

        Button addButton = new Button("+");
        addButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        addButton.setBackground(new Background(new BackgroundFill(Color.web("#4C7C8A"), new CornerRadii(50), null)));
        addButton.setTextFill(Color.WHITE);
        addButton.setPrefSize(30, 30);
        addButton.setAlignment(Pos.CENTER);
        addButton.setEffect(new DropShadow(5, 0, 1, Color.gray(0.2)));

        addButton.setOnMouseEntered(e -> {
            addButton.setBackground(new Background(new BackgroundFill(Color.web("#355B66"), new CornerRadii(50), null)));
            addButton.setScaleX(1.05);
            addButton.setScaleY(1.05);
        });
        addButton.setOnMouseExited(e -> {
            addButton.setBackground(new Background(new BackgroundFill(Color.web("#4C7C8A"), new CornerRadii(50), null)));
            addButton.setScaleX(1.0);
            addButton.setScaleY(1.0);
        });

        addButton.setOnAction(e -> showAddEditTaskDialog(null));

        filterBar.getChildren().addAll(titleLabel, spacer, sortCombo, calendarButton, addButton);
        return filterBar;
    }

    private void showCalendar() {
        if (CalendarApp.getCalendarStage() == null) {
            calendarApp = new CalendarApp();
            Stage calendarStage = new Stage();
            calendarApp.start(calendarStage);
        } else {
            CalendarApp.getCalendarStage().toFront();
        }
    }

    private HBox createProgressBarAndLabel() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(completionProgressBar, Priority.ALWAYS);

        completionProgressBar.setStyle("-fx-accent: #4C7C8A; -fx-background-color: #F5F8FA; -fx-background-radius: 4;");
        completionProgressBar.setPrefHeight(20);

        completionLabel.setFont(Font.font("Arial", 14));
        completionLabel.setTextFill(Color.web("#2D3E50"));

        box.getChildren().addAll(completionProgressBar, completionLabel);
        return box;
    }

    public void sortTasks(String sort) {
        switch (sort) {
            case "By Title":
                sortedTasks.setComparator((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                break;
            case "By Deadline":
                sortedTasks.setComparator((t1, t2) -> {
                    LocalDateTime d1 = t1.getEndTime() != null ? t1.getEndTime() : LocalDateTime.MAX;
                    LocalDateTime d2 = t2.getEndTime() != null ? t2.getEndTime() : LocalDateTime.MAX;
                    return d1.compareTo(d2);
                });
                break;
            case "By Priority":
                sortedTasks.setComparator((t1, t2) -> {
                    int p1 = getPriorityValue(t1.getPriority());
                    int p2 = getPriorityValue(t2.getPriority());
                    return Integer.compare(p2, p1); // Cao đến thấp
                });
                break;
            default:
                sortedTasks.setComparator(null); // Không sắp xếp
                break;
        }
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 3;
            case "Medium":
                return 2;
            case "Low":
                return 1;
            default:
                return 2;
        }
    }

    public void showAddEditTaskDialog(LinkTasksAndCalModel taskToEdit) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle(taskToEdit == null ? "Add Task" : "Edit Task");

        initIconAddTasksAndEdit(dialogStage, taskToEdit != null);

        if (taskToEdit != null) {
            if (taskToEdit.getId() == null || taskToEdit.getId() <= 0) {
                showAlert("Error", "Invalid task ID.");
                return;
            }
            taskToEdit = DAOTasks.getInstance().loadTaskById(taskToEdit.getId());
            if (taskToEdit == null) {
                showAlert("Error", "Task not found in the database.");
                return;
            }
        }
        TextField titleField = new TextField(taskToEdit != null ? taskToEdit.getTitle() : "");
        titleField.setFont(Font.font("Arial", 14));
        titleField.setPromptText("Enter task title");
        titleField.setStyle("-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 8;");
        titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            titleField.setStyle(newVal ? "-fx-background-color: #F5F8FA; -fx-border-color: #4C7C8A; -fx-border-radius: 4; -fx-padding: 8;" :
                    "-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 8;");
        });

        TextArea descriptionArea = new TextArea(taskToEdit != null ? taskToEdit.getDescription() : "");
        descriptionArea.setFont(Font.font("Arial", 14));
        descriptionArea.setPromptText("Add your task description");
        descriptionArea.setStyle("-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 8;");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            descriptionArea.setStyle(newVal ? "-fx-background-color: #F5F8FA; -fx-border-color: #4C7C8A; -fx-border-radius: 4; -fx-padding: 8;" :
                    "-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 8;");
        });

        DatePicker startDatePicker = new DatePicker(taskToEdit != null && taskToEdit.getStartTime() != null ? taskToEdit.getStartTime().toLocalDate() : null);
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, taskToEdit != null && taskToEdit.getStartTime() != null ? taskToEdit.getStartTime().toLocalTime().getHour() : 0);
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, taskToEdit != null && taskToEdit.getStartTime() != null ? taskToEdit.getStartTime().toLocalTime().getMinute() : 0);
        DatePicker endDatePicker = new DatePicker(taskToEdit != null && taskToEdit.getEndTime() != null ? taskToEdit.getEndTime().toLocalDate() : null);
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, taskToEdit != null && taskToEdit.getEndTime() != null ? taskToEdit.getEndTime().toLocalTime().getHour() : 23);
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, taskToEdit != null && taskToEdit.getEndTime() != null ? taskToEdit.getEndTime().toLocalTime().getMinute() : 59);

        startHourSpinner.setPrefWidth(80);
        startMinuteSpinner.setPrefWidth(80);
        endHourSpinner.setPrefWidth(80);
        endMinuteSpinner.setPrefWidth(80);
        startHourSpinner.setEditable(true);
        startMinuteSpinner.setEditable(true);
        endHourSpinner.setEditable(true);
        endMinuteSpinner.setEditable(true);
        styleSpinner(startHourSpinner);
        styleSpinner(startMinuteSpinner);
        styleSpinner(endHourSpinner);
        styleSpinner(endMinuteSpinner);

        addSpinnerValidation(startHourSpinner, 0, 23);
        addSpinnerValidation(startMinuteSpinner, 0, 59);
        addSpinnerValidation(endHourSpinner, 0, 23);
        addSpinnerValidation(endMinuteSpinner, 0, 59);

        Label deadlineLabel = new Label("Deadline");

        Spinner<Integer> durationSpinner = new Spinner<>(0, 1440, taskToEdit != null ? (int) taskToEdit.getDuration() : 0);
        durationSpinner.setPrefWidth(100);
        durationSpinner.setEditable(true);
        styleSpinner(durationSpinner);
        addSpinnerValidation(durationSpinner, 0, 1440);
        Label durationUnitLabel = new Label("mins");

        Spinner<Integer> timeSpentHoursSpinner = new Spinner<>(0, 23, taskToEdit != null ? taskToEdit.getTimeSpentHours() : 0);
        Spinner<Integer> timeSpentMinutesSpinner = new Spinner<>(0, 59, taskToEdit != null ? taskToEdit.getTimeSpentMinutes() : 0);
        timeSpentHoursSpinner.setPrefWidth(80);
        timeSpentMinutesSpinner.setPrefWidth(80);
        timeSpentHoursSpinner.setEditable(true);
        timeSpentMinutesSpinner.setEditable(true);
        styleSpinner(timeSpentHoursSpinner);
        styleSpinner(timeSpentMinutesSpinner);
        addSpinnerValidation(timeSpentHoursSpinner, 0, 23);
        addSpinnerValidation(timeSpentMinutesSpinner, 0, 59);
        Label timeSpentHoursLabel = new Label("hr");
        Label timeSpentMinutesLabel = new Label("mins");

        ComboBox<String> recurrenceCombo = new ComboBox<>();
        recurrenceCombo.getItems().addAll("Does not repeat", "Daily", "Weekly", "Monthly");
        recurrenceCombo.setValue(taskToEdit != null ? taskToEdit.getRecurrence() : "Does not repeat");
        styleComboBox(recurrenceCombo);

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("High", "Medium", "Low");
        priorityCombo.setValue(taskToEdit != null ? taskToEdit.getPriority() : "Medium");
        styleComboBox(priorityCombo);

        Button saveButton = new Button("Save");
        styleButton(saveButton, "#4C7C8A", "#FFFFFF", 8);
        Button cancelButton = new Button("Cancel");
        styleButton(cancelButton, "#4C7C8A", "#FFFFFF", 8);
        Button deleteButton = new Button("Delete task");
        styleButton(deleteButton, "#FF4C4C", "#FFFFFF", 8);
        deleteButton.setVisible(taskToEdit != null);

        //gán lại tham chiếu đến cùng một đối tượng.
        LinkTasksAndCalModel finalTaskToEdit = taskToEdit;

        saveButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                showAlert("Error", "Please enter a task title.");
                return;
            }
            if (finalTaskToEdit == null && sharedTasks.stream().anyMatch(t -> t.getTitle().equalsIgnoreCase(title))) {
                showAlert("Error", "A task with this title already exists.");
                return;
            }

            LocalDateTime startTime = startDatePicker.getValue() != null ?
                    startDatePicker.getValue().atTime(startHourSpinner.getValue(), startMinuteSpinner.getValue()) : null;
            LocalDateTime endTime = endDatePicker.getValue() != null ?
                    endDatePicker.getValue().atTime(endHourSpinner.getValue(), endMinuteSpinner.getValue()) : null;

            if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
                showAlert("Error", "End time cannot be before start time.");
                return;
            }

            try {
                if (finalTaskToEdit == null) {
                    LinkTasksAndCalModel newTask = new LinkTasksAndCalModel();
                    newTask.setTitle(title);
                    newTask.setDescription(descriptionArea.getText());
                    newTask.setStartTime(startTime);
                    newTask.setEndTime(endTime);
                    newTask.setCompleted(false);
                    newTask.setDuration(durationSpinner.getValue());
                    newTask.setTimeSpentHours(timeSpentHoursSpinner.getValue());
                    newTask.setTimeSpentMinutes(timeSpentMinutesSpinner.getValue());
                    newTask.setRecurrence(recurrenceCombo.getValue());
                    newTask.setPriority(priorityCombo.getValue());

                    System.out.println("Đang lưu nhiệm vụ từ giao diện với tiêu đề: " + newTask.getTitle());
                    DAOTasks.getInstance().saveTaskToDatabase(newTask, true);
                    sharedTasks.add(newTask);
                } else {
                    LinkTasksAndCalModel updatedTask = new LinkTasksAndCalModel();
                    updatedTask.setId(finalTaskToEdit.getId());

                    updatedTask.setTitle(!title.equals(finalTaskToEdit.getTitle()) ? title : null);
                    String description = descriptionArea.getText();
                    updatedTask.setDescription(description != null && !description.equals(finalTaskToEdit.getDescription()) ? description : null);
                    updatedTask.setStartTime(startTime != null && !startTime.equals(finalTaskToEdit.getStartTime()) ? startTime : null);
                    updatedTask.setEndTime(endTime != null && !endTime.equals(finalTaskToEdit.getEndTime()) ? endTime : null);
                    updatedTask.setDuration(durationSpinner.getValue() != finalTaskToEdit.getDuration() ? durationSpinner.getValue() : finalTaskToEdit.getDuration());
                    updatedTask.setTimeSpentHours(timeSpentHoursSpinner.getValue() != finalTaskToEdit.getTimeSpentHours() ? timeSpentHoursSpinner.getValue() : finalTaskToEdit.getTimeSpentHours());
                    updatedTask.setTimeSpentMinutes(timeSpentMinutesSpinner.getValue() != finalTaskToEdit.getTimeSpentMinutes() ? timeSpentMinutesSpinner.getValue() : finalTaskToEdit.getTimeSpentMinutes());
                    updatedTask.setRecurrence(recurrenceCombo.getValue() != null && !recurrenceCombo.getValue().equals(finalTaskToEdit.getRecurrence()) ? recurrenceCombo.getValue() : null);
                    updatedTask.setPriority(priorityCombo.getValue() != null && !priorityCombo.getValue().equals(finalTaskToEdit.getPriority()) ? priorityCombo.getValue() : null);
                    updatedTask.setCompleted(finalTaskToEdit.isCompleted());

                    System.out.println("Đang cập nhật nhiệm vụ với ID: " + updatedTask.getId() + ", tiêu đề: [" + title + "]");
                    DAOTasks.getInstance().saveTaskToDatabase(updatedTask, false);

                    finalTaskToEdit.setTitle(title);
                    finalTaskToEdit.setDescription(descriptionArea.getText());
                    finalTaskToEdit.setStartTime(startTime);
                    finalTaskToEdit.setEndTime(endTime);
                    finalTaskToEdit.setDuration(durationSpinner.getValue());
                    finalTaskToEdit.setTimeSpentHours(timeSpentHoursSpinner.getValue());
                    finalTaskToEdit.setTimeSpentMinutes(timeSpentMinutesSpinner.getValue());
                    finalTaskToEdit.setRecurrence(recurrenceCombo.getValue());
                    finalTaskToEdit.setPriority(priorityCombo.getValue());

                    int index = sharedTasks.indexOf(finalTaskToEdit);
                    if (index >= 0) {
                        sharedTasks.set(index, finalTaskToEdit);
                    } else {
                        System.out.println("Lỗi: Không tìm thấy task trong sharedTasks để cập nhật!");
                    }
                }

                // Làm mới giao diện sau khi lưu
                Platform.runLater(() -> {
                    updateCompletionProgress();
                    checkOverdueTasks();
                    if (sortCombo != null) {
                        sortTasks(sortCombo.getValue());
                    }
                    if (calendarApp != null) {
                        calendarApp.updateTasks(sharedTasks);
                    }
                });
                dialogStage.close();
            } catch (RuntimeException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                if (ex.getCause() instanceof SQLException && ex.getMessage().contains("UK_Title")) {
                    alert.setHeaderText("Tiêu đề nhiệm vụ trùng lặp");
                    alert.setContentText("Nhiệm vụ với tiêu đề " + title + " đã tồn tại. Vui lòng chọn tiêu đề khác.");
                } else {
                    alert.setHeaderText("Lỗi khi lưu nhiệm vụ");
                    alert.setContentText("Đã xảy ra lỗi: " + ex.getMessage());
                }
                alert.showAndWait();
            }
        });

        cancelButton.setOnAction(e -> dialogStage.close());

        //gán lại tham chiếu đến cùng một đối tượng.
        LinkTasksAndCalModel finalTaskToEdit1 = taskToEdit;

        deleteButton.setOnAction(e -> {
            if (finalTaskToEdit1 != null) {

                DAOTasks.getInstance().delete(finalTaskToEdit1);

                sharedTasks.remove(finalTaskToEdit1);
                updateCompletionProgress();
                checkOverdueTasks();
                dialogStage.close();
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setBackground(new Background(new BackgroundFill(Color.SEASHELL, null, null)));
        grid.setEffect(new DropShadow(10, 0, 2, Color.gray(0.2)));

        int row = 0;
        grid.addRow(row++, new Label("Title:"), titleField);
        grid.addRow(row++, new Label("Description:"), descriptionArea);
        grid.addRow(row++, new Label("Start:"), new HBox(5, startDatePicker, startHourSpinner, new Label(":"), startMinuteSpinner));
        grid.addRow(row++, deadlineLabel, new HBox(5, endDatePicker, endHourSpinner, new Label(":"), endMinuteSpinner));
        grid.addRow(row++, new Label("Duration:"), new HBox(5, durationSpinner, durationUnitLabel));
        grid.addRow(row++, new Label("Time spent:"), new HBox(5, timeSpentHoursSpinner, timeSpentHoursLabel, timeSpentMinutesSpinner, timeSpentMinutesLabel));
        grid.addRow(row++, new Label("Recurrence:"), recurrenceCombo);
        grid.addRow(row++, new Label("Priority:"), priorityCombo);
        grid.addRow(row++, deleteButton, new HBox(5, saveButton, cancelButton));

        GridPane.setColumnSpan(titleField, 2);
        GridPane.setColumnSpan(descriptionArea, 2);
        GridPane.setHalignment(saveButton, HPos.RIGHT);
        GridPane.setHalignment(cancelButton, HPos.RIGHT);
        GridPane.setHalignment(deleteButton, HPos.LEFT);

        for (int i = 0; i < grid.getChildren().size(); i++) {
            if (grid.getChildren().get(i) instanceof Label) {
                Label label = (Label) grid.getChildren().get(i);
                label.setFont(Font.font("Arial", 14));
                label.setTextFill(Color.web("#2D3E50"));
            }
        }

        Scene dialogScene = new Scene(grid);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void initIconAddTasksAndEdit(Stage stage, boolean isEditMode){
        try {
            String iconPath = isEditMode ? "/IconEditTasks.png" : "/IconAddTasks.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            stage.getIcons().clear(); // Xóa các biểu tượng cũ (nếu có)
            stage.getIcons().add(icon);
        }catch (NullPointerException e){
            System.err.println("Error: Icon file not found. Please ensure " +
                    (isEditMode ? "edit_icon.png" : "add_icon.png") +
                    " exists in src/main/resources.");
        }
    }

    private void addSpinnerValidation(Spinner<Integer> spinner, int min, int max) {
        TextField editor = spinner.getEditor();
        editor.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                int value = Integer.parseInt(newValue);
                if (value < min) {
                    spinner.getValueFactory().setValue(min);
                    editor.setText(String.valueOf(min));
                } else if (value > max) {
                    spinner.getValueFactory().setValue(max);
                    editor.setText(String.valueOf(max));
                }
            } catch (NumberFormatException e) {
                if (!newValue.isEmpty()) {
                    spinner.getValueFactory().setValue(spinner.getValueFactory().getValue());
                    editor.setText(String.valueOf(spinner.getValue()));
                }
            }
        });
        editor.setOnAction(e -> {
            try {
                int value = Integer.parseInt(editor.getText());
                spinner.getValueFactory().setValue(Math.min(max, Math.max(min, value)));
            } catch (NumberFormatException ex) {
                editor.setText(String.valueOf(spinner.getValue()));
            }
        });
    }

    private void updateCompletionProgress() {
        long completedCount = sharedTasks.stream().filter(LinkTasksAndCalModel::isCompleted).count();
        completionProgressBar.setProgress(sharedTasks.isEmpty() ? 0 : (double) completedCount / sharedTasks.size());
        completionLabel.setText(completedCount + "/" + sharedTasks.size());
        checkOverdueTasks();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setBackground(new Background(new BackgroundFill(Color.SEASHELL, null, null)));
        alert.getDialogPane().setEffect(new DropShadow(10, 0, 2, Color.gray(0.2)));
        alert.showAndWait();
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class TaskListCell extends ListCell<LinkTasksAndCalModel> {
        private final Label titleLabel = new Label();
        private final Label timeLabel = new Label();
        private final VBox content = new VBox(5);
        private final ContextMenu contextMenu = new ContextMenu();
        private final MenuItem editItem = new MenuItem("Edit");
        private final MenuItem duplicateItem = new MenuItem("Duplicate");
        private final MenuItem deleteItem = new MenuItem("Delete");
        private final MenuItem scheduleItem = new MenuItem("Schedule Task");

        public TaskListCell() {
            content.setPadding(new Insets(10));
            content.setBackground(new Background(new BackgroundFill(Color.web("#F5F8FA"), new CornerRadii(8), null)));
            content.setBorder(new Border(new BorderStroke(Color.web("#E0ECEF"), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
            content.setEffect(new DropShadow(5, 0, 1, Color.gray(0.1)));

            HBox header = new HBox(10, titleLabel);
            header.setAlignment(Pos.CENTER_LEFT);
            content.getChildren().addAll(header, timeLabel);

            content.setOnMouseEntered(e -> {
                content.setEffect(new DropShadow(10, 0, 2, Color.gray(0.2)));
                content.setScaleX(1.01);
                content.setScaleY(1.01);
            });
            content.setOnMouseExited(e -> {
                content.setEffect(new DropShadow(5, 0, 1, Color.gray(0.1)));
                content.setScaleX(1.0);
                content.setScaleY(1.0);
            });

            contextMenu.getItems().addAll(editItem, duplicateItem, deleteItem, scheduleItem);
            setOnMouseClicked(e -> {
                if (!isEmpty() && e.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(this, e.getScreenX(), e.getScreenY());
                } else {
                    contextMenu.hide();
                }
            });

            editItem.setOnAction(e -> showAddEditTaskDialog(getItem()));
            duplicateItem.setOnAction(e -> {
                LinkTasksAndCalModel original = getItem();
                if (original != null) {
                    LinkTasksAndCalModel newTask = new LinkTasksAndCalModel();
                    newTask.setTitle(original.getTitle() + " (Copy)");
                    newTask.setDescription(original.getDescription());
                    newTask.setStartTime(original.getStartTime());
                    newTask.setEndTime(original.getEndTime());
                    newTask.setCompleted(false);
                    newTask.setDuration(original.getDuration());
                    newTask.setTimeSpentHours(original.getTimeSpentHours());
                    newTask.setTimeSpentMinutes(original.getTimeSpentMinutes());
                    newTask.setRecurrence(original.getRecurrence());
                    newTask.setPriority(original.getPriority());
                    DAOTasks.getInstance().saveTaskToDatabase(newTask,true);
                    sharedTasks.add(newTask);
                    updateCompletionProgress();
                }
            });
            deleteItem.setOnAction(e -> {
                LinkTasksAndCalModel task = getItem();
                if (task != null) {
                    DAOTasks.getInstance().delete(task);
                    sharedTasks.remove(task);
                    updateCompletionProgress();
                }
            });
            scheduleItem.setOnAction(e -> {
                LinkTasksAndCalModel task = getItem();
                if (task != null) showAddEditTaskDialog(task);
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    LinkTasksAndCalModel draggedTask = sharedTasks.get(draggedIndex);
                    int dropIndex = sharedTasks.indexOf(getItem());
                    if (dropIndex < 0 || dropIndex >= sharedTasks.size()) {
                        dropIndex = sharedTasks.size() - 1;
                    }
                    sharedTasks.remove(draggedIndex);
                    sharedTasks.add(dropIndex, draggedTask);
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }

        @Override
        protected void updateItem(LinkTasksAndCalModel linkTasksAndCalModel, boolean empty) {
            super.updateItem(linkTasksAndCalModel, empty);
            if (empty || linkTasksAndCalModel == null) {
                setGraphic(null);
                setContextMenu(null);
            } else {
                titleLabel.setText(linkTasksAndCalModel.getTitle() + " [" + linkTasksAndCalModel.getPriority() + "]");
                titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                titleLabel.setTextFill(Color.web("#2D3E50"));

                String timeText = "";
                boolean isOverdue = linkTasksAndCalModel.getEndTime() != null && LocalDateTime.now().isAfter(linkTasksAndCalModel.getEndTime()) && !linkTasksAndCalModel.isCompleted();
                if (linkTasksAndCalModel.getStartTime() != null && linkTasksAndCalModel.getEndTime() != null) {
                    timeText = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(linkTasksAndCalModel.getStartTime()) +
                            " - " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(linkTasksAndCalModel.getEndTime());
                } else if (linkTasksAndCalModel.getEndTime() != null) {
                    timeText = "Hết hạn: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(linkTasksAndCalModel.getEndTime());
                } else {
                    timeText = "No scheduled time";
                }
                timeLabel.setText(timeText);
                timeLabel.setFont(Font.font("Arial", 12));
                timeLabel.setTextFill(isOverdue ? Color.RED : Color.web("#2D3E50"));

                if (isOverdue) {
                    content.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2))));
                } else {
                    content.setBorder(new Border(new BorderStroke(Color.web("#E0ECEF"), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
                }

                setGraphic(content);
                setContextMenu(contextMenu);
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

    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 6;");
        comboBox.setPrefWidth(150);
        comboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            comboBox.setStyle(newVal ? "-fx-background-color: #F5F8FA; -fx-border-color: #4C7C8A; -fx-border-radius: 4; -fx-padding: 6;" :
                    "-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 6;");
        });
    }

    private void styleSpinner(Spinner<?> spinner) {
        spinner.setStyle("-fx-background-color: #F5F8FA; -fx-border-color: #B0BEC5; -fx-border-radius: 4; -fx-padding: 6;");
        spinner.getEditor().setFont(Font.font("Arial", 14));
    }
}
