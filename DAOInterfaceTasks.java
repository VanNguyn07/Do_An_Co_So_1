package DAO;

import LinkTasksAndCal.LinkTasksAndCalModel;
import javafx.collections.ObservableList;

import java.util.List;

public interface DAOInterfaceTasks<T> {
    public int delete(T obj);


    public int update(T obj);

    public LinkTasksAndCalModel loadTaskById(Integer id);

    public ObservableList<LinkTasksAndCalModel> selectAllTasks();

    public void saveTaskToDatabase(T obj, boolean isNew);

    public  boolean doesTitleExist(String title);

    public List<LinkTasksAndCalModel> searchTasksByTitle(String title);

}
