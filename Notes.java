
package Note;

import DAO.DAONotes;
import Model.NotesModel;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Optional;

public class Notes extends Application {

    public Button buttonBold, buttonItalic, buttonLowercase, buttonSave, buttonList, buttonDelete;
    public TextField textFieldSave;
    private ImageView iconBold, iconItalic, iconLowercase, iconSave, iconList, iconDelete;
    private TextArea textAreaMain;
    public ArrayList<NotesModel> textFieldsList = new ArrayList<>();
    public  NotesList notesList;
    private NotesModel editingNotesModel;
    private Notes parentNotes;// Thêm trường để lưu tham chiếu đến đối tượng Notes ban đầu

    public Notes(NotesModel notesModel, Notes parentNotes) {
        // Làm mới editingNotesModel từ cơ sở dữ liệu
        this.editingNotesModel = DAONotes.getInstance().selectAll()
                .stream()
                .filter(model -> model.getName().equals(notesModel.getName()))
                .findFirst()
                .orElse(notesModel);
        this.parentNotes = parentNotes;
        if (parentNotes != null) {
            this.textFieldsList = new ArrayList<>(parentNotes.textFieldsList);
        }
    }

    public Notes() {
        this.editingNotesModel = null;
        this.parentNotes = null;
        // Tải dữ liệu từ cơ sở dữ liệu cho đối tượng chính
        this.textFieldsList = DAONotes.getInstance().selectAll();
    }

    @Override
    public void start(Stage stage) throws Exception {
        VBox vBox = new VBox();
        vBox.setPrefWidth(400);
        vBox.setPrefHeight(300);
        vBox.setAlignment(Pos.CENTER);
        vBox.setBackground(new Background(new BackgroundFill(Color.SEASHELL, new CornerRadii(5), new Insets(0))));

        if(editingNotesModel != null) {
            initButtonHBoxDelete(vBox, editingNotesModel);
        }else {
            initButtonHBox(vBox);
        }

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle(editingNotesModel != null ? editingNotesModel.getName() : "Notes");
        initIconNotes(stage);
        loadNotesFromDatabase();
        stage.show();
    }

    public void initIconNotes(Stage stage) {
        Image imageNotes = new Image(getClass().getResourceAsStream("/IconsNotes.png"));
        stage.getIcons().add(imageNotes);
    }

    public void effectMouse(Button button) {
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

    }

    public void initButtonHBoxDelete(VBox vBox, NotesModel notesModel) {
        iconLowercase = new ImageView(new Image(getClass().getResourceAsStream("/InconLowercase.png")));
        iconLowercase.setFitWidth(30);
        iconLowercase.setFitHeight(30);

        buttonLowercase = new Button("", iconLowercase);
        buttonLowercase.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonLowercase);

        buttonLowercase.setOnAction(event -> {
            Font currentFont = buttonLowercase.getFont();
            double sizeCurrent = currentFont.getSize();
            textAreaMain.setFont(Font.font(currentFont.getFamily(), FontWeight.NORMAL, sizeCurrent));
        });

        iconBold = new ImageView(new Image(getClass().getResourceAsStream("/IconsBold.png")));
        iconBold.setFitWidth(30);
        iconBold.setFitHeight(30);

        buttonBold = new Button("", iconBold);
        buttonBold.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonBold);

        buttonBold.setOnAction(event -> {
            Font currentFont = buttonBold.getFont();
            String nameFontCurrent = currentFont.getName();
            double sizeFontCurrent = currentFont.getSize();
            boolean isBold = currentFont.getStyle().toLowerCase().contains("bold");
            if (isBold) {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontWeight.NORMAL, sizeFontCurrent));
            } else {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontWeight.BOLD, sizeFontCurrent));
            }
        });

        iconItalic = new ImageView(new Image(getClass().getResourceAsStream("/IconsItalic.png")));
        iconItalic.setFitWidth(30);
        iconItalic.setFitHeight(30);

        buttonItalic = new Button("", iconItalic);
        buttonItalic.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonItalic);

        buttonItalic.setOnAction(event -> {
            Font currentFont = buttonItalic.getFont();
            String nameFontCurrent = currentFont.getName();
            double sizeFontCurrent = currentFont.getSize();
            boolean isItalic = currentFont.getStyle().toLowerCase().contains("italic");
            if (isItalic) {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontWeight.NORMAL, sizeFontCurrent));
            } else {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontPosture.ITALIC, sizeFontCurrent));
            }
        });

        iconSave = new ImageView(new Image(getClass().getResourceAsStream("/IconsSave.png")));
        iconSave.setFitWidth(30);
        iconSave.setFitHeight(30);

        buttonSave = new Button("", iconSave);
        buttonSave.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonSave);

        buttonSave.setOnAction(event -> {
                saveNotes();
        });

        iconDelete = new ImageView(new Image(getClass().getResourceAsStream("/IconDele.png")));
        iconDelete.setFitWidth(30);
        iconDelete.setFitHeight(30);

        buttonDelete = new Button("", iconDelete);
        buttonDelete.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonDelete);

        buttonDelete.setOnAction(event -> {
          deleteNotes();
        });

        textFieldSave = new TextField();
        textFieldSave.setPromptText("Name your notes!");
        textFieldSave.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        textFieldSave.setPrefWidth(130);
        textFieldSave.setPrefHeight(30);

        // Điền tên và vô hiệu hóa chỉnh sửa
        if (editingNotesModel != null) {
            textFieldSave.setText(editingNotesModel.getName());
            textFieldSave.setDisable(true);
        }

        HBox hBox = new HBox(5, buttonLowercase, buttonBold, buttonItalic, textFieldSave, buttonSave, buttonDelete);
        hBox.setAlignment(Pos.CENTER);

        textAreaMain = new TextArea();
        textAreaMain.setPromptText("Start typing a notes...");
        textAreaMain.setWrapText(true);
        textAreaMain.setPrefHeight(270);
        textAreaMain.setPrefWidth(400);

        // Điền nội dung
        if(editingNotesModel != null) {
            textAreaMain.setText(editingNotesModel.getContent());
        }
        vBox.setSpacing(0);
        vBox.getChildren().addAll(hBox, textAreaMain);
    }

    public void initButtonHBox(VBox vBox) {
        iconLowercase = new ImageView(new Image(getClass().getResourceAsStream("/InconLowercase.png")));
        iconLowercase.setFitWidth(30);
        iconLowercase.setFitHeight(30);

        buttonLowercase = new Button("", iconLowercase);
        buttonLowercase.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonLowercase);

        buttonLowercase.setOnAction(event -> {
            Font currentFont = buttonLowercase.getFont();
            double sizeCurrent = currentFont.getSize();
            textAreaMain.setFont(Font.font(currentFont.getFamily(), FontWeight.NORMAL, sizeCurrent));
        });

        iconBold = new ImageView(new Image(getClass().getResourceAsStream("/IconsBold.png")));
        iconBold.setFitWidth(30);
        iconBold.setFitHeight(30);

        buttonBold = new Button("", iconBold);
        buttonBold.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonBold);

        buttonBold.setOnAction(event -> {
            Font currentFont = buttonBold.getFont();
            String nameFontCurrent = currentFont.getName();
            double sizeFontCurrent = currentFont.getSize();
            boolean isBold = currentFont.getStyle().toLowerCase().contains("bold");
            if (isBold) {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontWeight.NORMAL, sizeFontCurrent));
            } else {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontWeight.BOLD, sizeFontCurrent));
            }
        });

        iconItalic = new ImageView(new Image(getClass().getResourceAsStream("/IconsItalic.png")));
        iconItalic.setFitWidth(30);
        iconItalic.setFitHeight(30);

        buttonItalic = new Button("", iconItalic);
        buttonItalic.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonItalic);

        buttonItalic.setOnAction(event -> {
            Font currentFont = buttonItalic.getFont();
            String nameFontCurrent = currentFont.getName();
            double sizeFontCurrent = currentFont.getSize();
            boolean isItalic = currentFont.getStyle().toLowerCase().contains("italic");
            if (isItalic) {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontWeight.NORMAL, sizeFontCurrent));
            } else {
                textAreaMain.setFont(Font.font(nameFontCurrent, FontPosture.ITALIC, sizeFontCurrent));
            }
        });

        iconSave = new ImageView(new Image(getClass().getResourceAsStream("/IconsSave.png")));
        iconSave.setFitWidth(30);
        iconSave.setFitHeight(30);

        buttonSave = new Button("", iconSave);
        buttonSave.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonSave);

        buttonSave.setOnAction(event -> {
            saveNotes();
        });

        iconList = new ImageView(new Image(getClass().getResourceAsStream("/IconsList.png")));
        iconList.setFitWidth(30);
        iconList.setFitHeight(30);

        buttonList = new Button("", iconList);
        buttonList.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        effectMouse(buttonList);

        buttonList.setOnAction(event -> {
            if (notesList == null || !notesList.isShowing()) {
                try {
                    notesList = new NotesList(this);
                    notesList.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                notesList.getStage().requestFocus();
            }
        });

        textFieldSave = new TextField();
        textFieldSave.setPromptText("Name your notes!");
        textFieldSave.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15));
        textFieldSave.setPrefWidth(130);
        textFieldSave.setPrefHeight(30);

        if(editingNotesModel != null){
            textFieldSave.setText(editingNotesModel.getName());
        }
        HBox hBox = new HBox(5, buttonLowercase, buttonBold, buttonItalic, textFieldSave, buttonSave, buttonList);
        hBox.setAlignment(Pos.CENTER);

        textAreaMain = new TextArea();
        textAreaMain.setPromptText("Start typing a notes...");
        textAreaMain.setWrapText(true);
        textAreaMain.setPrefHeight(270);
        textAreaMain.setPrefWidth(400);

        if(editingNotesModel != null){
            textAreaMain.setText(editingNotesModel.getContent());
        }
        vBox.setSpacing(0);
        vBox.getChildren().addAll(hBox, textAreaMain);
    }

    public void saveNotes(){
        String name = textFieldSave.getText();
        String content = textAreaMain.getText();

        if(name.isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter notes name!","/IconInfor.png");
            return;
        }
            NotesModel newNotesModel = new NotesModel(name, content);

            if(editingNotesModel != null){
                // Cập nhật ghi chú hiện có
                textFieldSave.setText(editingNotesModel.getName());
                textAreaMain.setText(editingNotesModel.getContent());

                newNotesModel.setName(editingNotesModel.getName()); // Ensure name doesn't change
                DAONotes.getInstance().update(newNotesModel);

                // Làm mới textFieldsList của parentNotes từ cơ sở dữ liệu
                if (parentNotes != null) {
                    parentNotes.textFieldsList = DAONotes.getInstance().selectAll();
                }

                // Nếu không có parentNotes (đối tượng chính), làm mới textFieldsList của đối tượng hiện tại
                if (parentNotes == null) {
                    this.textFieldsList = DAONotes.getInstance().selectAll();
                }

                // Update in-memory list
//                for(int i = 0; i < textFieldsList.size(); i++){
//                    if(textFieldsList.get(i).getName().equals(newNotesModel.getName())){
//                        textFieldsList.set(i, newNotesModel);
//                        break;
//                    }
//                }

                // Cập nhật textFieldsList của parentNotes (notesMain)
//                if(parentNotes != null){
//                    for(int i = 0; i < textFieldsList.size(); i++){
//                        if(textFieldsList.get(i).getName().equals(newNotesModel.getName())){
//                            textFieldsList.set(i, newNotesModel);
//                            break;
//                        }
//                    }
//                }
                showAlert(Alert.AlertType.INFORMATION, "Success","Notes \"" + editingNotesModel.getName()+ "\" updated successfully","/IconSuccess.png");

                // Cập nhật NotesList nếu đang mở
                NotesList currentNotesList = (parentNotes != null && parentNotes.notesList != null) ? parentNotes.notesList : notesList;
                if (currentNotesList != null && currentNotesList.isShowing()) {
                    currentNotesList.rebuildNotesList();
                }

                textFieldsList.clear();
                textAreaMain.clear();
                // Đóng cửa sổ chỉnh sửa
                Stage stage = (Stage) buttonSave.getScene().getWindow();
                stage.close();

            }else {
                // Check for duplicate name
                if (textFieldsList.stream().anyMatch(model -> model.getName().equals(name))) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Notes name \"" + name + "\" existed!", "/IconInfor.png");
                    return;
                }

                if(notesList == null || !notesList.isShowing()){
                    showAlert(Alert.AlertType.INFORMATION, "Warning", "You need open list!", "/IconInfor.png");
                }
            }

        // Update NotesList if open
        if (notesList != null && notesList.isShowing()) {
            notesList.addNodeIntoList(newNotesModel);
            // Insert new note
            DAONotes.getInstance().insert(newNotesModel);
            textFieldsList.add(newNotesModel);// Thêm vào danh sách trong bộ nhớ

            // Cập nhật parentNotes nếu có
            if(parentNotes != null){
                parentNotes.textFieldsList.add(newNotesModel);
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Notes \"" + name + "\" saved!", "/IconSuccess.png");
            textFieldSave.clear();
            textAreaMain.clear();


        }
    }

    public void deleteNotes(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this \"" + editingNotesModel.getName() + "\"?");

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/IconDele.png")));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DAONotes.getInstance().delete(editingNotesModel);

                // Làm mới textFieldsList từ cơ sở dữ liệu
                if (parentNotes != null) {
                    parentNotes.textFieldsList = DAONotes.getInstance().selectAll();
                } else {
                    this.textFieldsList = DAONotes.getInstance().selectAll();
                }

                textFieldsList.removeIf(model -> model.getName().equals(editingNotesModel.getName()));// Xóa khỏi danh sách trong bộ nhớ

                // Sử dụng notesList từ parentNotes nếu có, nếu không thì từ đối tượng hiện tại
                NotesList currentNotesList = (parentNotes != null && parentNotes.notesList != null) ? parentNotes.notesList : null;
                if(currentNotesList != null && currentNotesList.isShowing()){
                    System.out.println("Gọi removeButton từ deleteNotes");
                    currentNotesList.removeButton(editingNotesModel);
                }else {
                    System.out.println("currentNotesList không khả dụng: " + (currentNotesList == null ? "null" : "không hiển thị"));
                }

                // Đóng cửa sổ chỉnh sửa sau khi xóa
                Stage stage = (Stage) buttonDelete.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
        }
    }

    private void loadNotesFromDatabase() {
        textFieldsList.clear();
        textFieldsList.addAll(DAONotes.getInstance().selectAll());
    }

    public ArrayList<NotesModel> getTextFieldsList(){
        return textFieldsList;
    }

    public void showAlert(Alert.AlertType type, String title, String content, String iconPart) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream(iconPart)));

        alert.showAndWait();
    }
}

class NotesList extends Application {
    private Notes notesMain;
    private VBox vbox;
    private Stage stage;

    public NotesList(Notes notes) {
        this.notesMain = notes;
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        rebuildNotesList();

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 150, 200);
        stage.setScene(scene);
        initIconNotes(stage);
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }

    public void initIconNotes(Stage stage) {
        Image imageNotes = new Image(getClass().getResourceAsStream("/IconsNotes.png"));
        stage.getIcons().add(imageNotes);
    }

    public void addNodeIntoList(NotesModel notesModel) {
        Button button = new Button(notesModel.getName());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            Notes editNotes = new Notes(notesModel, notesMain);
            try {
                editNotes.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        vbox.getChildren().add(button);
        System.out.println("Đã thêm nút: " + notesModel.getName());
    }

    public void removeButton(NotesModel notesModel) {
        System.out.println("Bắt đầu xóa nút cho: " + notesModel.getName());
        System.out.println("Số nút hiện tại trong vbox: " + vbox.getChildren().size());

        for (Node node : new ArrayList<>(vbox.getChildren())) {
            if (node instanceof Button button) {
                System.out.println("Kiểm tra nút: '" + button.getText() + "' so với '" + notesModel.getName() + "'");
                if (button.getText().equals(notesModel.getName())) {
                    vbox.getChildren().remove(node);
                    System.out.println("Đã xóa nút: " + button.getText());
                    break;
                }
            }
        }

        System.out.println("Sau khi xóa, số nút trong vbox: " + vbox.getChildren().size());
    }

    public void rebuildNotesList() {
        vbox.getChildren().clear();
        for (NotesModel model : notesMain.textFieldsList) {
            Button button = new Button(model.getName());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> {
                Notes editNotes = new Notes(model,notesMain);
                try {
                    editNotes.start(new Stage());
                }catch (Exception e) {
                    e.printStackTrace();
                }
            });
            vbox.getChildren().add(button);
        }
    }

    public boolean isShowing() {
        return stage != null && stage.isShowing();
    }
}
