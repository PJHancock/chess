package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.Memory.*;
import model.GameData;
import model.UserData;
import service.requests.*;
import service.results.*;

import java.util.Collection;
import java.util.Objects;

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
        Collection<ListGamesData> games = game.listGames();

        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException {
        if (createGameRequest == null || createGameRequest.gameName() == null || createGameRequest.gameName().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
        if (!auth.getAuth(createGameRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        int gameID = game.createGame(createGameRequest.gameName());
        return new CreateGameResult(gameID);
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
        // System.out.println("test point 0");
        if (joinGameRequest == null || joinGameRequest.gameID() <= 0 ||
                joinGameRequest.playerColor() == null ||
                ((joinGameRequest.playerColor() != ChessGame.TeamColor.WHITE) && (joinGameRequest.playerColor() != ChessGame.TeamColor.BLACK))) {
            // System.out.println("test point 1");
            throw new DataAccessException("Error: bad request");
        }
        // System.out.println("test point 2");
        if (!auth.getAuth(joinGameRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        String username = auth.getUser(joinGameRequest.authToken());
        game.updateGame(username, joinGameRequest.playerColor(), joinGameRequest.gameID());
        return null;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        if (registerRequest == null ||
                registerRequest.username() == null || registerRequest.username().isEmpty() ||
                registerRequest.password() == null || registerRequest.password().isEmpty() ||
                registerRequest.email() == null || registerRequest.email().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
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
        if (!auth.getAuth(logoutRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        auth.deleteAuth(logoutRequest.authToken());
        return null;
    }
}
