package dataaccess;

import chess.ChessGame;
import model.GameData;
import dataaccess.DataAccessException;

public interface GameDAO {

    void clear() throws DataAccessException;

    void createGame(String gameName) throws DataAccessException;

    void getGame(String gameID) throws DataAccessException;

    void listGames() throws DataAccessException;;

    void updateGame(ChessGame.TeamColor teamColor, String gameName) throws DataAccessException;;

}
