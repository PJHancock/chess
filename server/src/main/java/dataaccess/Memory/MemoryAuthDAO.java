package dataaccess.Memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

public class MemoryAuthDAO implements AuthDAO {
    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public String generateToken() throws DataAccessException {
        return "";
    }

    @Override
    public void getAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void deleteAuth() throws DataAccessException {

    }
}
