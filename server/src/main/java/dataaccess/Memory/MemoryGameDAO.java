package dataaccess.Memory;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public void clear(){
        games.clear();
    }

    public void createGame(String gameName) throws DataAccessException {

    }

    public void getGame(String gameID) throws DataAccessException {

    }

    public void listGames() throws DataAccessException {

    }

    public void updateGame(ChessGame.TeamColor teamColor, String gameName) throws DataAccessException {

    }
}
