package dataaccess.Memory;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    final private HashMap<Integer, UserDAO> users = new HashMap<>();

    public void clear(){
        users.clear();
    }

    public void createUser(UserData u) throws DataAccessException {

    }

    public void getUser(String username) throws DataAccessException {

    }
}
