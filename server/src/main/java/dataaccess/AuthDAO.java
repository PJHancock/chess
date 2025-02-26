package dataaccess;

public interface AuthDAO {

    void clear();

    String generateToken() throws DataAccessException;

    void getAuth(String authToken) throws DataAccessException;

    void deleteAuth() throws DataAccessException;

}

