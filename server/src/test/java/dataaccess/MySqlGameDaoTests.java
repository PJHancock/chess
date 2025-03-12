package dataaccess;

import chess.ChessGame;
import dataaccess.sql.MySqlGameDao;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.results.ListGamesData;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MySqlGameDaoTests {

    private MySqlGameDao testDataBase;

    @BeforeEach
    void setUp() throws DataAccessException {
        testDataBase = new MySqlGameDao();
        testDataBase.clear();
    }

    @Test
    void clear() throws DataAccessException {
        testDataBase.createGame("testGame");
        assertNotNull(testDataBase.getGame("testGame"));
        testDataBase.clear();
        assertNull(testDataBase.getGame("testGame"));
    }

    @Test
    void getGamePositive() throws DataAccessException {
        testDataBase.createGame("testGame");
        assertNotNull(testDataBase.getGame("testGame"));
    }

    @Test
    void getGameNegative() throws DataAccessException {
        testDataBase.createGame("testGame");
        assertNotNull(testDataBase.getGame("testGame"));
        assertThrows(DataAccessException.class, () -> testDataBase.getGame(null),
                "Should throw DataAccessException for null request");
    }

    @Test
    void createGamePositive() throws DataAccessException {
        int gameID = testDataBase.createGame("testGame");
        GameData testGameInfo = testDataBase.getGame("testGame");
        assertEquals(gameID, testGameInfo.gameID());
    }

    @Test
    void createGameNegative() {
        assertThrows(DataAccessException.class, () -> testDataBase.createGame(null),
                "Should throw DataAccessException for null request");
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        var testGamesList = new ArrayList<ListGamesData>();
        assertEquals(testGamesList, testDataBase.listGames());
        testDataBase.createGame("testGame1");
        testDataBase.createGame("testGame2");
        assertNotNull(testDataBase.listGames());
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        assertNotNull(testDataBase.listGames());
        assertEquals(0, testDataBase.listGames().size());
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        int gameID = testDataBase.createGame("testGame");
        GameData gameInfo = testDataBase.getGame("testGame");
        assertNull(gameInfo.whiteUsername());
        testDataBase.updateGame("testUser", ChessGame.TeamColor.WHITE, gameID);
        GameData updatedGameInfo = testDataBase.getGame("testGame");
        assertEquals("testUser", updatedGameInfo.whiteUsername());
    }

    @Test
    void updateGameNegative() throws DataAccessException {
        int gameID = testDataBase.createGame("testGame");
        assertThrows(DataAccessException.class, () -> testDataBase.updateGame(null, ChessGame.TeamColor.WHITE, gameID),
                "Should throw DataAccessException for null request");
        assertThrows(DataAccessException.class, () -> testDataBase.updateGame("testUser", ChessGame.TeamColor.WHITE, 0),
                "Should throw DataAccessException for null request");
    }
}