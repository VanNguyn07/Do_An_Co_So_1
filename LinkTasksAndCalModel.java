package LinkTasksAndCal;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class LinkTasksAndCalModel {
    private final IntegerProperty id = new SimpleIntegerProperty(-1); // Sử dụng int, -1 là giá trị mặc định khi chưa có id
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDateTime> startTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> endTime = new SimpleObjectProperty<>();
    private final LongProperty duration = new SimpleLongProperty(0); // Duration in minutes
    private final IntegerProperty timeSpentHours = new SimpleIntegerProperty(0);
    private final IntegerProperty timeSpentMinutes = new SimpleIntegerProperty(0);
    private final StringProperty recurrence = new SimpleStringProperty("Does not repeat");
    private final BooleanProperty completed = new SimpleBooleanProperty(false);
    private final StringProperty priority = new SimpleStringProperty("Medium"); // Thêm priority

    // Constructor hiện tại
    public LinkTasksAndCalModel(String title, LocalDateTime startTime, LocalDateTime endTime, boolean completed) {
        this.title.set(title);
        this.startTime.set(startTime);
        this.endTime.set(endTime);
        this.completed.set(completed);
    }
    public LinkTasksAndCalModel(){

    }
    // Constructor đầy đủ với tất cả các thuộc tính
    public LinkTasksAndCalModel(int id, String title, String description, LocalDateTime startTime, LocalDateTime endTime,
                                boolean completed, long duration, int timeSpentHours, int timeSpentMinutes,
                                String recurrence, String priority) {
        this.id.set(id);
        this.title.set(title);
        this.description.set(description);
        this.startTime.set(startTime);
        this.endTime.set(endTime);
        this.completed.set(completed);
        this.duration.set(duration);
        this.timeSpentHours.set(timeSpentHours);
        this.timeSpentMinutes.set(timeSpentMinutes);
        this.recurrence.set(recurrence);
        this.priority.set(priority);
    }


    public Integer getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public LocalDateTime getStartTime() {
        return startTime.get();
    }

    public ObjectProperty<LocalDateTime> startTimeProperty() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime.set(startTime);
    }

    public LocalDateTime getEndTime() {
        return endTime.get();
    }

    public ObjectProperty<LocalDateTime> endTimeProperty() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime.set(endTime);
    }

    public long getDuration() {
        return duration.get();
    }

    public LongProperty durationProperty() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration.set(duration);
    }

    public int getTimeSpentHours() {
        return timeSpentHours.get();
    }

    public IntegerProperty timeSpentHoursProperty() {
        return timeSpentHours;
    }

    public void setTimeSpentHours(int hours) {
        this.timeSpentHours.set(hours);
    }

    public int getTimeSpentMinutes() {
        return timeSpentMinutes.get();
    }

    public IntegerProperty timeSpentMinutesProperty() {
        return timeSpentMinutes;
    }

    public void setTimeSpentMinutes(int minutes) {
        this.timeSpentMinutes.set(minutes);
    }

    public String getRecurrence() {
        return recurrence.get();
    }

    public StringProperty recurrenceProperty() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence.set(recurrence);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    public String getPriority() {
        return priority.get();
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }
}