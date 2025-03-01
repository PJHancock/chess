package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.memory.*;
import model.UserData;
import service.requests.*;
import service.results.*;
import java.util.List;

public class ChessService {
    private final MemoryAuthDao auth = new MemoryAuthDao();
    private final MemoryGameDao game = new MemoryGameDao();
    private final MemoryUserDao user = new MemoryUserDao();

    public ClearResult clear() {
        auth.clear();
        game.clear();
        user.clear();
        return null;
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
        if (!auth.getAuth(listGamesRequest.authToken())) {
            throw new DataAccessException("Error: unauthorized");
        }
        List<ListGamesData> gameList = game.listGames();

        return new ListGamesResult(gameList);
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
        if (joinGameRequest == null || joinGameRequest.gameID() <= 0 ||
                joinGameRequest.playerColor() == null ||
                ((joinGameRequest.playerColor() != ChessGame.TeamColor.WHITE) && (joinGameRequest.playerColor() != ChessGame.TeamColor.BLACK))) {
            throw new DataAccessException("Error: bad request");
        }
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
