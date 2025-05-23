package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DatabaseManager;
import java.sql.*;
import java.util.UUID;

public class MySqlAuthDao implements AuthDAO {

    public MySqlAuthDao() throws dataaccess.DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  auth (
              `id` int NOT NULL AUTO_INCREMENT,
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(authToken),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws dataaccess.DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    public void clear() throws dataaccess.DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM auth")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to clear auth table: %s", e.getMessage()));
        }
    }

    public String generateToken(String username) throws dataaccess.DataAccessException {
        String authToken = UUID.randomUUID().toString();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO auth (authToken, username) VALUES(?, ?)";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.setString(2, username);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to generate token: %s", e.getMessage()));
        }
        return authToken;
    }

    public boolean getAuth(String authToken) throws dataaccess.DataAccessException {
        if (authToken == null) {
            throw new dataaccess.DataAccessException("Invalid request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM auth WHERE authToken = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try (var resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next(); // Returns true if authToken exists
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to get authToken: %s", e.getMessage()));
        }
    }

    public void deleteAuth(String authToken) throws dataaccess.DataAccessException {
        if (authToken == null) {
            throw new dataaccess.DataAccessException("Invalid request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM auth WHERE authToken = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to delete authToken: %s", e.getMessage()));
        }
    }

    public String getUser(String authToken) throws dataaccess.DataAccessException {
        if (authToken == null) {
            throw new dataaccess.DataAccessException("Invalid request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM auth WHERE authToken = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("username");
                    } else {
                        return null; // No user found for the given authToken
                    }
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to get username: %s", e.getMessage()));
        }
    }
}

