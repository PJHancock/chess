package dataaccess.Memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    final private HashMap<Integer, AuthData> authTokens = new HashMap<>();
    private int nextID = 1;

    public void clear() {
        authTokens.clear();
    }

    public String generateToken(String username) {
        String authToken = UUID.randomUUID().toString();
        authTokens.put(nextID, new AuthData(username, authToken));
        nextID++;
        return authToken;
    }

    public boolean getAuth(String authToken) {
        return authTokens.containsValue(authToken);
    }

    public void deleteAuth() throws DataAccessException {

    }
}
