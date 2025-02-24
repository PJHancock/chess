package dataaccess;

import model.AuthData;
import dataaccess.DataAccessException;

import java.util.UUID;

public class AuthDAO {

    public void clear() {

    }

    // unsure if this should be here in the model class
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public void getAuth(String authToken) {
    }

    public void deleteAuth() {
    }

}

