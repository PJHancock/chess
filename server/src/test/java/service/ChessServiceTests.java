package service;

import dataaccess.memory.MemoryAuthDao;
import dataaccess.memory.MemoryGameDao;
import dataaccess.memory.MemoryUserDao;
import model.UserData;
import org.junit.jupiter.api.*;
import passoff.model.TestAuthResult;
import passoff.model.TestCreateRequest;
import passoff.model.TestUser;
import passoff.server.TestServerFacade;
import server.Server;

public class ChessServiceTests {

    private static MemoryAuthDao authTest;
    private static MemoryGameDao gameTest;
    private static MemoryUserDao userTest;

    @AfterAll
    static void clearData() {
        authTest.clear();
        gameTest.clear();
        userTest.clear();
    }

    @BeforeAll
    public static void init() {
        authTest = new MemoryAuthDao();
        gameTest = new MemoryGameDao();
        userTest = new MemoryUserDao();
    }

    @Test
    @DisplayName("Clear Positive Test")
    public void clearPositiveTest() {
        authTest.generateToken("Test Name");
        gameTest.createGame("Test Game");
        userTest.createUser(new UserData("TestName", "TestPassword", "TestEmail"));
        authTest.clear();
        gameTest.clear();
        userTest.clear();
        // Assertions.assertTrue(authTest);

    }

    @Test
    @DisplayName("Create Game Positive Test")
    public void createGamePositiveTest() {

    }

    @Test
    @DisplayName("Create Game Negative Test")
    public void createGameNegativeTest() {

    }

    @Test
    @DisplayName("Join Game Positive Test")
    public void joinGamePositiveTest() {

    }

    @Test
    @DisplayName("Join Game Negative Test")
    public void joinGameNegativeTest() {

    }

    @Test
    @DisplayName("List Games Positive Test")
    public void listGamesPositiveTest() {

    }

    @Test
    @DisplayName("List Games Negative Test")
    public void listGamesNegativeTest() {

    }

    @Test
    @DisplayName("Login Positive Test")
    public void loginPositiveTest() {

    }

    @Test
    @DisplayName("Login Negative Test")
    public void loginNegativeTest() {

    }

    @Test
    @DisplayName("Logout Positive Test")
    public void logoutPositiveTest() {

    }

    @Test
    @DisplayName("Logout Negative Test")
    public void logoutNegativeTest() {

    }

    @Test
    @DisplayName("Register Positive Test")
    public void registerPositiveTest() {

    }

    @Test
    @DisplayName("Register Negative Test")
    public void registerNegativeTest() {

    }
}
