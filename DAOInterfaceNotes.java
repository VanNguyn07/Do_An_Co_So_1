package DAO;

import Model.NotesModel;

import java.util.ArrayList;

public interface DAOInterfaceNotes<T> {
        public int delete(T obj);

        public int insert(T obj);

        public int update(T obj);

        public ArrayList<NotesModel> selectAll();
}

