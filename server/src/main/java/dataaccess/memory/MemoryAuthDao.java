package dataaccess.memory;

import dataaccess.AuthDAO;
import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDao implements AuthDAO {
    final private HashMap<String, AuthData> authTokens = new HashMap<>();

    public void clear() {
        authTokens.clear();
    }

    public String generateToken(String username) {
        String authToken = UUID.randomUUID().toString();
        authTokens.put(authToken, new AuthData(authToken, username));
        return authToken;
    }

    public boolean getAuth(String authToken) {
        return authTokens.containsKey(authToken);
    }

    public void deleteAuth(String authToken) throws dataaccess.DataAccessException {
        if (!authTokens.containsKey(authToken)) {
            throw new dataaccess.DataAccessException("Error: Auth token not found");
        }
        authTokens.remove(authToken);
    }

    public String getUser(String authToken) {
        AuthData authData = authTokens.get(authToken);
        return authData.username();
    }
}
