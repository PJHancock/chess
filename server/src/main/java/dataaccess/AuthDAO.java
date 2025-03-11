package dataaccess;

public interface AuthDAO {

    void clear() throws DataAccessException;

    String generateToken(String username) throws DataAccessException;

    boolean getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    String getUser(String authToken) throws DataAccessException;
}

