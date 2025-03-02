package dataaccess;

public interface AuthDAO {

    int getAuthTokens();

    void clear();

    String generateToken(String username);

    boolean getAuth(String authToken);

    void deleteAuth(String authToken) throws DataAccessException;

    String getUser(String authToken);
}

