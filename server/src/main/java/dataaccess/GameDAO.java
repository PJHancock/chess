package dataaccess;

import chess.ChessGame;
import service.results.ListGamesData;
import java.util.List;

public interface GameDAO {

    void clear();

    int createGame(String gameName);

    List<ListGamesData> listGames();

    void updateGame(String username, ChessGame.TeamColor teamColor, int gameName) throws DataAccessException;

}
