package dataaccess;

import model.UserData;

public interface UserDAO {

    void clear();

    void createUser(UserData u) throws DataAccessException;

    void getUser(String username) throws DataAccessException;

}
