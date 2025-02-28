package dataaccess.Memory;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import service.results.ListGamesData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    final private HashMap<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    public void clear(){
        games.clear();
    }

    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextID;
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(nextID, game);
        nextID++;
        return gameID;
    }

    public boolean getGame(int gameID) throws DataAccessException {
        for (GameData game : games.values()) {
            if (game.gameID() == gameID) {
                return true;
            }
        }
        return false;
    }

    public Collection<ListGamesData> listGames() throws DataAccessException {
        Collection<ListGamesData> game_list = new ArrayList<>();
        for (HashMap.Entry<Integer, GameData> game : games.entrySet()) {
            GameData originalGame = game.getValue();
            ListGamesData modifiedGame = new ListGamesData(originalGame.gameID(), originalGame.whiteUsername(),
                                                           originalGame.blackUsername(), originalGame.gameName());
            game_list.add(modifiedGame);
        }
        return game_list;
    }

    public void updateGame(String username, ChessGame.TeamColor teamColor, int gameID) throws DataAccessException {
        nextID++;
        GameData game = games.get(gameID);
        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            games.put(nextID, new GameData(nextID, username, game.blackUsername(), game.gameName(), new ChessGame()));
        } else {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            games.put(nextID, new GameData(nextID, game.whiteUsername(), username, game.gameName(), new ChessGame()));
        }
        games.remove(gameID);
    }
}
