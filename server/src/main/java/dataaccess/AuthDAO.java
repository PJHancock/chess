package dataaccess;
import java.util.UUID;

public interface AuthDAO {

    void clear() throws DataAccessException;

    String generateToken() throws DataAccessException;

    void getAuth(String authToken) throws DataAccessException;

    void deleteAuth() throws DataAccessException;

}

