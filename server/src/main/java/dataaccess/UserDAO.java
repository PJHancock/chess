package dataaccess;

import model.UserData;

public interface UserDAO {

    void clear() throws DataAccessException;

    void createUser(UserData u) throws DataAccessException;

    void getUser(String username) throws DataAccessException;

}
