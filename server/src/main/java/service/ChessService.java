package service;

import dataaccess.DataAccessException;
import dataaccess.Memory.*;
import model.GameData;
import model.UserData;
import service.requests.*;
import service.results.*;

import java.util.Collection;

public class ChessService {
    private final MemoryAuthDAO auth = new MemoryAuthDAO();
    private final MemoryGameDAO game = new MemoryGameDAO();
    private final MemoryUserDAO user = new MemoryUserDAO();

    public ClearResult clear(ClearRequest request) {
        auth.clear();
        game.clear();
        user.clear();
        return null;
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
        if (!auth.getAuth(listGamesRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        Collection<GameData> games = game.listGames();

        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException {
        if (!auth.getAuth(createGameRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        int gameID = game.createGame(createGameRequest.gameName());
        return new CreateGameResult(gameID);
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
        if (!auth.getAuth(joinGameRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        String username = auth.getUser(joinGameRequest.authToken());
        game.updateGame(username, joinGameRequest.playerColor(), joinGameRequest.gameID());
        return null;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        if (user.getUser(registerRequest.username())) {
            throw new DataAccessException("Error: already taken");
        }
        user.createUser(new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email()));
        String authToken = auth.generateToken(registerRequest.username());
        return new RegisterResult(registerRequest.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        if (!user.verifyUser(loginRequest.username(), loginRequest.password())) {
            throw new DataAccessException("Error: unauthorized");
        }
        String authToken = auth.generateToken(loginRequest.username());
        return new LoginResult(loginRequest.username(), authToken);
    }

    public LogoutResult logout(LogoutRequest logoutRequest) throws DataAccessException {
        if (auth.getAuth(logoutRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        auth.deleteAuth(logoutRequest.authToken());
        return null;
    }
}
