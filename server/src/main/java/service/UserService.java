package service;

import dataaccess.DataAccessException;
import dataaccess.Memory.MemoryAuthDAO;
import dataaccess.Memory.MemoryUserDAO;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.LogoutResult;
import service.results.RegisterResult;

public class UserService {
    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        MemoryAuthDAO auth = new MemoryAuthDAO();
        MemoryUserDAO user = new MemoryUserDAO();
        if (user.getUser(registerRequest.userData().username())) {
            throw new DataAccessException("Error: already taken");
        }
        user.createUser(registerRequest.userData());
        String authToken = auth.generateToken(registerRequest.userData().username());
        return new RegisterResult(registerRequest.userData().username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }

    public LogoutResult logout(LogoutRequest logoutRequest) {
        throw new UnsupportedOperationException("This method has not been implemented yet");
    }
}
