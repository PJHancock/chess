package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.*;
import model.UserData;
import service.requests.*;
import service.results.*;
import java.util.List;

public class ChessService {

    private final AuthDAO auth;
    private final GameDAO game;
    private final UserDAO user;

    public ChessService(AuthDAO auth, GameDAO game, UserDAO user) {
        this.auth = auth;
        this.game = game;
        this.user = user;
    }

    public ClearResult clear() throws dataaccess.DataAccessException {
        auth.clear();
        game.clear();
        user.clear();
        return new ClearResult();
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws dataaccess.DataAccessException {
        if (!auth.getAuth(listGamesRequest.authToken())) {
            throw new dataaccess.DataAccessException("Error: unauthorized");
        }
        List<ListGamesData> gameList = game.listGames();

        return new ListGamesResult(gameList);
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws dataaccess.DataAccessException {
        if (createGameRequest == null || createGameRequest.gameName() == null || createGameRequest.gameName().isEmpty()) {
            throw new dataaccess.DataAccessException("Error: bad request");
        }
        if (!auth.getAuth(createGameRequest.authToken())) {
            throw new dataaccess.DataAccessException("Error: unauthorized");
        }
        int gameID = game.createGame(createGameRequest.gameName());
        return new CreateGameResult(gameID);
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) throws dataaccess.DataAccessException {
        if (joinGameRequest == null || joinGameRequest.gameID() <= 0 ||
                joinGameRequest.playerColor() == null ||
                ((joinGameRequest.playerColor() != ChessGame.TeamColor.WHITE) && (joinGameRequest.playerColor() != ChessGame.TeamColor.BLACK))) {
            throw new dataaccess.DataAccessException("Error: bad request");
        }
        if (!auth.getAuth(joinGameRequest.authToken())) {
            throw new dataaccess.DataAccessException("Error: unauthorized");
        }
        String username = auth.getUser(joinGameRequest.authToken());
        game.updateGame(username, joinGameRequest.playerColor(), joinGameRequest.gameID());
        return null;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws dataaccess.DataAccessException {
        if (registerRequest == null ||
                registerRequest.username() == null || registerRequest.username().isEmpty() ||
                registerRequest.password() == null || registerRequest.password().isEmpty() ||
                registerRequest.email() == null || registerRequest.email().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
        if (user.getUser(registerRequest.username())) {
            throw new dataaccess.DataAccessException("Error: already taken");
        }
        user.createUser(new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email()));
        String authToken = auth.generateToken(registerRequest.username());
        return new RegisterResult(registerRequest.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws dataaccess.DataAccessException {
        if (!user.verifyUser(loginRequest.username(), loginRequest.password())) {
            throw new dataaccess.DataAccessException("Error: unauthorized");
        }
        String authToken = auth.generateToken(loginRequest.username());
        return new LoginResult(loginRequest.username(), authToken);
    }

    public LogoutResult logout(LogoutRequest logoutRequest) throws dataaccess.DataAccessException {
        if (!auth.getAuth(logoutRequest.authToken())) {
            throw new dataaccess.DataAccessException("Error: unauthorized");
        }
        auth.deleteAuth(logoutRequest.authToken());
        return null;
    }
}
