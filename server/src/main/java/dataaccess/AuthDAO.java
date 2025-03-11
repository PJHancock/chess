package dataaccess;

public interface AuthDAO {

    void clear();

    String generateToken(String username);

    boolean getAuth(String authToken);

    void deleteAuth(String authToken) throws DataAccessException;

    String getUser(String authToken);
}

