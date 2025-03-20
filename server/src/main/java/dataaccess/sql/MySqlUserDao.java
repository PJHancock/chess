package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class MySqlUserDao implements UserDAO {

    public MySqlUserDao() throws dataaccess.DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NULL,
              `password` varchar(64) NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(username),
              UNIQUE(username),
              INDEX(password),
              INDEX(email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws dataaccess.DataAccessException {
        DatabaseManager.configureDatabase(createStatements);
    }

    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM users")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to clear auth table: %s", e.getMessage()));
        }
    }

    public void createUser(UserData u) throws dataaccess.DataAccessException {
        if (u.username() == null || u.password() == null || u.email() == null) {
            throw new dataaccess.DataAccessException("bad request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO users (username, password, email) VALUES(?, ?, ?)";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, u.username());
                String hashedPassword = BCrypt.hashpw(u.password(), BCrypt.gensalt());
                preparedStatement.setString(2, hashedPassword);
                preparedStatement.setString(3, u.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to create user: %s", e.getMessage()));
        }
    }

    public boolean getUser(String username) throws dataaccess.DataAccessException {
        if (username == null) {
            throw new dataaccess.DataAccessException("bad request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM users WHERE username = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                try (var resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next(); // Returns true if username exists
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to get user: %s", e.getMessage()));
        }
    }

    public boolean verifyUser(String username, String password) throws dataaccess.DataAccessException {
        if (username == null || password == null) {
            throw new dataaccess.DataAccessException("bad request");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password FROM users WHERE username = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String hashedPassword = resultSet.getString("password"); // Retrieve hashed password
                        return BCrypt.checkpw(password, hashedPassword); // Securely compare passwords
                    }
                }
            }
        } catch (SQLException e) {
            throw new dataaccess.DataAccessException(String.format("Unable to verify user: %s", e.getMessage()));
        }
        return false;
    }
}
