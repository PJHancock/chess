package dataaccess.memory;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.results.ListGamesData;

import java.util.*;

public class MemoryGameDao implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    public GameData getGame(String gameName) {
        for (GameData game : games.values()) {
            if (game.gameName().equals(gameName)) {
                return game;
            }
        }
        return null; // Return null if no matching game is found
    }

    public void clear(){
        games.clear();
    }

    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
        int gameID = nextID;
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(nextID, game);
        nextID++;
        return gameID;
    }

    public List<ListGamesData> listGames() {
        if (games.isEmpty()) {
            return new ArrayList<>();
        }

        List<ListGamesData> gameList = new ArrayList<>();

        for (GameData game : games.values()) {
            gameList.add(new ListGamesData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }
        return gameList;
    }

    public void updateGame(String username, ChessGame.TeamColor teamColor, int gameID) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Error: already taken");
        }
        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new dataaccess.DataAccessException("Error: already taken");
            }
            game = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
        } else {
            if (game.blackUsername() != null) {
                throw new dataaccess.DataAccessException("Error: already taken");
            }
            game = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
        }
        games.put(gameID, game);
    }
}
