package dataaccess.SQL;

import dataaccess.UserDAO;
import model.UserData;

import java.util.HashMap;

public class MySqlUserDao implements UserDAO {
    final private HashMap<Integer, UserData> users = new HashMap<>();
    private int nextID = 1;

    public int getUsers() {
        return users.size();
    }

    public void clear(){
        users.clear();
    }

    public void createUser(UserData u) {
        UserData user = new UserData(u.username(), u.password(), u.email());
        users.put(nextID, user);
        nextID++;
    }

    public boolean getUser(String username) {
        for (UserData user : users.values()) {
            if (user.username().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyUser(String username, String password) {
        if (getUser(username)) {
            for (UserData user : users.values()) {
                if (user.password().equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }
}
