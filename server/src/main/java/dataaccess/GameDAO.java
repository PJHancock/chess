package dataaccess;

import chess.ChessGame;
import model.GameData;
import service.results.ListGamesData;

import java.util.Collection;

public interface GameDAO {

    void clear();

    int createGame(String gameName) throws DataAccessException;

    boolean getGame(int gameID) throws DataAccessException;

    Collection<ListGamesData> listGames() throws DataAccessException;;

    void updateGame(String username, ChessGame.TeamColor teamColor, int gameName) throws DataAccessException;;

}
