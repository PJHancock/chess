package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDao;
import dataaccess.memory.MemoryGameDao;
import dataaccess.memory.MemoryUserDao;
import model.UserData;
import org.junit.jupiter.api.*;
import service.requests.*;
import service.results.CreateGameResult;
import service.results.ListGamesData;
import service.results.LoginResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChessServiceTests {

    private static ChessService chessServiceTest;
    private static MemoryAuthDao authTest;
    private static MemoryGameDao gameTest;
    private static MemoryUserDao userTest;

    @BeforeEach
    public void init() {
        authTest = new MemoryAuthDao();
        gameTest = new MemoryGameDao();
        userTest = new MemoryUserDao();
        chessServiceTest = new ChessService(authTest, gameTest, userTest); // Pass shared instances
    }

    @AfterEach
    public void reset() {
        authTest.clear();
        gameTest.clear();
        userTest.clear();
    }

    @Test
    @DisplayName("Clear Positive Test")
    public void clearPositiveTest() throws DataAccessException {
        String authToken = authTest.generateToken("TestName");
        gameTest.createGame("TestGame");
        userTest.createUser(new UserData("TestUser", "TestPassword", "TestEmail"));

        chessServiceTest.clear();

        assertFalse(authTest.getAuth(authToken));
        assertFalse(userTest.getUser("TestUser"));
        assertNull(gameTest.getGame("TestGame"));
    }

    @Test
    @DisplayName("Create Game Positive Test")
    public void createGamePositiveTest() throws DataAccessException {
        String username = "TestUser";
        String authToken = authTest.generateToken(username);
        CreateGameRequest request = new CreateGameRequest("NewChessGame", authToken);

        CreateGameResult result = chessServiceTest.createGame(request);

        assertNotNull(result, "CreateGameResult should not be null");
        assertTrue(result.gameID() > 0, "Game ID should be a positive integer");
        assertNotNull(gameTest.getGame("NewChessGame"), "Game should exist in gameTest");
    }

    @Test
    @DisplayName("Create Game Negative Test")
    public void createGameNegativeTest() {
        assertThrows(DataAccessException.class, () -> {
            chessServiceTest.createGame(null);
        }, "Should throw DataAccessException for null request");
    }

    @Test
    @DisplayName("Join Game Positive Test")
    public void joinGamePositiveTest() throws DataAccessException {
        gameTest.createGame("TestGame");
        String username = "TestUser";
        String authToken = authTest.generateToken(username);
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE,1, authToken);

        chessServiceTest.joinGame(request);

        assertEquals("TestUser", gameTest.getGame("TestGame").whiteUsername());
        assertNull(gameTest.getGame("TestGame").blackUsername());
    }

    @Test
    @DisplayName("Join Game Negative Test")
    public void joinGameNegativeTest() throws DataAccessException {
        gameTest.createGame("TestGame");
        String username = "TestUser";
        String authToken = authTest.generateToken(username);
        //incorrect gameID
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE,2, authToken);

        assertThrows(DataAccessException.class, () -> {
            chessServiceTest.joinGame(request);
        }, "Should throw DataAccessException for incorrect gameID");
    }

    @Test
    @DisplayName("List Games Positive Test")
    public void listGamesPositiveTest() throws DataAccessException {
        List<ListGamesData> gameList = new ArrayList<>();
        gameList.add(new ListGamesData(1, null, null, "TestGame1"));
        gameList.add(new ListGamesData(2, null, null, "TestGame2"));
        gameTest.createGame("TestGame1");
        gameTest.createGame("TestGame2");

        String username = "TestUser";
        String authToken = authTest.generateToken(username);
        assertEquals(gameList, chessServiceTest.listGames(new ListGamesRequest(authToken)).games());
    }

    @Test
    @DisplayName("List Games Negative Test")
    public void listGamesNegativeTest() {
        ListGamesRequest request = new ListGamesRequest(null);

        assertThrows(DataAccessException.class, () -> {
            chessServiceTest.listGames(request);
        }, "Should throw DataAccessException for unauthorized");
    }

    @Test
    @DisplayName("Login Positive Test")
    public void loginPositiveTest() throws DataAccessException {
        userTest.createUser(new UserData("TestUsername", "TestPassword", "TestEmail"));
        LoginRequest request = new LoginRequest("TestUsername", "TestPassword");
        LoginResult result = chessServiceTest.login(request);
        assertNotNull(result.authToken());
    }

    @Test
    @DisplayName("Login Negative Test")
    public void loginNegativeTest() {
        userTest.createUser(new UserData("TestUsername", "TestPassword", "TestEmail"));
        LoginRequest request = new LoginRequest("TestUsername", "WrongPassword");
        assertThrows(DataAccessException.class, () -> {
            chessServiceTest.login(request);
        }, "Should throw DataAccessException for wrong password");
    }

    @Test
    @DisplayName("Logout Positive Test")
    public void logoutPositiveTest() throws DataAccessException {
        userTest.createUser(new UserData("TestUsername", "TestPassword", "TestEmail"));
        String authToken = authTest.generateToken("TestUsername");
        chessServiceTest.logout(new LogoutRequest(authToken));
        assertFalse(authTest.getAuth(authToken));
    }

    @Test
    @DisplayName("Logout Negative Test")
    public void logoutNegativeTest() {
        assertThrows(DataAccessException.class, () -> {
            chessServiceTest.logout(new LogoutRequest(null));
        }, "Should throw DataAccessException for unauthorized");
    }

    @Test
    @DisplayName("Register Positive Test")
    public void registerPositiveTest() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("TestUsername", "TestPassword", "TestEmail");
        chessServiceTest.register(request);
        assertTrue(userTest.verifyUser("TestUsername", "TestPassword"));
    }

    @Test
    @DisplayName("Register Negative Test")
    public void registerNegativeTest() {
        RegisterRequest request = new RegisterRequest("TestUsername", "TestPassword", null);
        assertThrows(DataAccessException.class, () -> {
            chessServiceTest.register(request);
        }, "Should throw DataAccessException for null email");
    }
}
