package dataaccess.Memory;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    final private HashMap<Integer, UserData> users = new HashMap<>();
    private int nextID = 1;

    public void clear(){
        users.clear();
    }

    public void createUser(UserData u) throws DataAccessException {
        UserData user = new UserData(u.username(), u.password(), u.email());
        users.put(nextID, user);
        nextID++;
    }

    public boolean getUser(String username) throws DataAccessException {
        return users.containsValue(username);
    }
}
