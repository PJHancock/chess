package dataaccess;

import model.*;
import dataaccess.DataAccessException;

import java.util.UUID;

public class AuthDAO {

    void clear() {
    }

    // unsure if this should be here in the model class
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    void getAuth(String authToken) {
    }

    void deleteAuth() {
    }

}

