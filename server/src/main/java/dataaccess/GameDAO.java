package dataaccess;

import chess.ChessGame;
import model.GameData;
import service.results.ListGamesData;

import java.util.Collection;
import java.util.List;

public interface GameDAO {

    void clear();

    int createGame(String gameName) throws DataAccessException;

    boolean getGame(int gameID) throws DataAccessException;

    List<ListGamesData> listGames() throws DataAccessException;;

    void updateGame(String username, ChessGame.TeamColor teamColor, int gameName) throws DataAccessException;;

}
