package dataaccess;

import model.UserData;

public interface UserDAO {

    int getUsers();

    void clear();

    void createUser(UserData u);

    boolean getUser(String username);

    boolean verifyUser(String username, String password);

}
