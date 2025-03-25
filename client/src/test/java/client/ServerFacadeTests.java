package client;


import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;
import static server.Server.chessService;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        var url = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + url);
        facade = new ServerFacade(url);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        chessService.clear();
    }

    @Test
    void registerPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerNegative() {
        assertThrows(ui.DataAccessException.class, () -> facade.register(null, "password", "p1@email.com"),
                "Should throw DataAccessException for null request");
    }

    @Test
    void loginPositive() throws ui.DataAccessException {
        var authData = facade.register("player1", "password", "p1@email.com");
        facade.logout(authData.authToken());
        var authData2 = facade.login("player1", "password");
        assertTrue(authData2.authToken().length() > 10);
    }

    @Test
    void loginNegative() throws ui.DataAccessException {
        var authData = facade.register("player1", "password", "p1@email.com");
        facade.logout(authData.authToken());
        assertThrows(ui.DataAccessException.class, () -> facade.login("player1", "otherPassword"),
                "Should throw DataAccessException for bad request");
    }

    @Test
    void createPositive() throws ui.DataAccessException {
        var authData = facade.register("player1", "password", "p1@email.com");
        int gameId = facade.create(authData.authToken(), "game");
        assertTrue(gameId > 0);
    }

    @Test
    void createNegative() throws ui.DataAccessException {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertThrows(ui.DataAccessException.class, () -> facade.create(authData.authToken(), null),
                "Should throw DataAccessException for bad request");
    }

    @Test
    void listPositive() throws ui.DataAccessException {
        var authData = facade.register("player1", "password", "p1@email.com");
        facade.create(authData.authToken(), "game1");
        facade.create(authData.authToken(), "game2");
        facade.create(authData.authToken(), "game3");
        assertEquals(3, facade.list(authData.authToken()).size());
    }

    @Test
    void listNegative() throws ui.DataAccessException {
        facade.register("player1", "password", "p1@email.com");
        assertThrows(ui.DataAccessException.class, () -> facade.list(null),
                "Should throw DataAccessException for unauthorized request");
    }

    @Test
    void joinPositive() {

    }

    @Test
    void joinNegative() {
    }

    @Test
    void logoutPositive() {
    }

    @Test
    void logoutNegative() {
    }

}