package dataaccess;

import model.UserData;

public interface UserDAO {

    void clear() throws DataAccessException;

    void createUser(UserData u) throws DataAccessException;

    boolean getUser(String username) throws DataAccessException;

    boolean verifyUser(String username, String password) throws DataAccessException;

}
