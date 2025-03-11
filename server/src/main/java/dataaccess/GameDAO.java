package dataaccess;

import chess.ChessGame;
import model.GameData;
import service.results.ListGamesData;
import java.util.List;

public interface GameDAO {

    GameData getGame(String gameName) throws DataAccessException;

    void clear() throws DataAccessException;

    int createGame(String gameName) throws DataAccessException;

    List<ListGamesData> listGames() throws DataAccessException;

    void updateGame(String username, ChessGame.TeamColor teamColor, int gameName) throws DataAccessException;

}
