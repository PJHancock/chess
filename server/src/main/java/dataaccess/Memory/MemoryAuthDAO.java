package dataaccess.Memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    final private HashMap<Integer, AuthData> authTokens = new HashMap<>();

    public void clear() {
        authTokens.clear();
    }

    public String generateToken() throws DataAccessException {
        return "";
    }

    public void getAuth(String authToken) throws DataAccessException {

    }

    public void deleteAuth() throws DataAccessException {

    }
}
