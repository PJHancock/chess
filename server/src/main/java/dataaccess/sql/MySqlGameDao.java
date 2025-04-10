package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;
import model.results.ListGamesData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class MySqlGameDao implements GameDAO {

    public MySqlGameDao() throws dataaccess.DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  game (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256) NULL,
              `blackUsername` varchar(256) NULL,
              `gameName` varchar(256) NOT NULL,
              `chessGame` longtext NOT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(whiteUsername),
              INDEX(blackUsername),
              INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws dataaccess.DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    public void clear() throws dataaccess.DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM game")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to clear auth table: %s", e.getMessage()));
        }
    }

    public GameData getGame(String gameName) throws dataaccess.DataAccessException {
        if (gameName == null) {
            throw new dataaccess.DataAccessException("Invalid request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameName = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, gameName);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int gameID = resultSet.getInt("gameID");
                    String whiteUsername = resultSet.getString("whiteUsername");
                    String blackUsername = resultSet.getString("blackUsername");
                    String json = resultSet.getString("chessGame");
                    var chessGame = new Gson().fromJson(json, ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to get game: %s", e.getMessage()));
        }
    }

    public GameData getGameUsingId(String gameId) throws dataaccess.DataAccessException {
        if (gameId == null) {
            throw new dataaccess.DataAccessException("Invalid request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, gameId);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int gameID = resultSet.getInt("gameID");
                    String whiteUsername = resultSet.getString("whiteUsername");
                    String blackUsername = resultSet.getString("blackUsername");
                    String gameName = resultSet.getString("gameName");
                    String json = resultSet.getString("chessGame");
                    var chessGame = new Gson().fromJson(json, ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to get game: %s", e.getMessage()));
        }
    }


    public int createGame(String gameName) throws dataaccess.DataAccessException {
        if (gameName == null) {
            throw new dataaccess.DataAccessException("Invalid request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, chessGame) VALUES(?, ?, ?, ?)";
            try (var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, null);
                preparedStatement.setString(2, null);
                preparedStatement.setString(3, gameName);
                ChessGame chessGame = new ChessGame();
                var json = new Gson().toJson(chessGame);
                preparedStatement.setString(4, json);
                preparedStatement.executeUpdate();
                try (var resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1); // Get the generated gameID
                    } else {
                        throw new dataaccess.DataAccessException("Game ID was not generated.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to create game: %s", e.getMessage()));
        }
    }

    public List<ListGamesData> listGames() throws dataaccess.DataAccessException {
        var games = new ArrayList<ListGamesData>();
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName FROM game")) {
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        var gameID = rs.getInt("gameID");
                        var whiteUsername = rs.getString("whiteUsername");
                        var blackUsername = rs.getString("blackUsername");
                        var gameName = rs.getString("gameName");
                        games.add(new ListGamesData(gameID, whiteUsername, blackUsername, gameName));
                    }
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to list games: %s", e.getMessage()));
        }
        return games;
    }

    public void updateGameUsername(String username, ChessGame.TeamColor teamColor, int gameID) throws dataaccess.DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                var resultSet = preparedStatement.executeQuery();
                GameData game = null;
                if (resultSet.next()) {
                    String whiteUsername = resultSet.getString("whiteUsername");
                    String blackUsername = resultSet.getString("blackUsername");
                    String gameName = resultSet.getString("gameName");
                    String json = resultSet.getString("chessGame");
                    var chessGame = new Gson().fromJson(json, ChessGame.class);
                    game = new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                }

                if (game == null) {
                    throw new dataaccess.DataAccessException("Error: Game not found");
                }

                // Update username if needed
                var statement2 = getUsernameUpdateQuery(username, teamColor, game);
                try (var preparedStatement2 = conn.prepareStatement(statement2)) {
                    preparedStatement2.setString(1, username);
                    preparedStatement2.setInt(2, gameID);
                    preparedStatement2.executeUpdate();
                } catch (SQLException e) {
                    throw new dataaccess.DataAccessException("Unable to update game: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException("Unable to update game: " + e.getMessage());
        }
    }

    private static String getUsernameUpdateQuery(String username, ChessGame.TeamColor teamColor, GameData game)
            throws dataaccess.DataAccessException {
        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (username != null && game.whiteUsername() != null && !Objects.equals(username, game.whiteUsername())) {
                throw new dataaccess.DataAccessException("Error: already taken");
            }
            return "UPDATE game SET whiteUsername=? WHERE gameID=?";
        } else {
            if (username != null && game.blackUsername() != null && !Objects.equals(username, game.blackUsername())) {
                throw new dataaccess.DataAccessException("Error: already taken");
            }
            return "UPDATE game SET blackUsername=? WHERE gameID=?";
        }
    }

    public void updateGameBoard(ChessGame updatedGame, int gameID) throws dataaccess.DataAccessException {
        var statement = "UPDATE game SET chessGame=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            String serializedGame = new Gson().toJson(updatedGame);
            preparedStatement.setString(1, serializedGame);
            preparedStatement.setInt(2, gameID);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new dataaccess.DataAccessException("Unable to update game board: " + e.getMessage());
        }
    }
}
