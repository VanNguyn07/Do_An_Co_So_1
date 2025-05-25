package Model;

public class NotesModel {
    private String name;
    private String content;

    public NotesModel(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public NotesModel(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }
}
