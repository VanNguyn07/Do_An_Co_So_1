package DAO;

public interface DAOInterfaceUser<T> {
    public int insert(T obj);

    public boolean isUsernameExist(String username);
}
