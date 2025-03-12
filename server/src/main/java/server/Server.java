package server;

import chess.ChessGame;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import dataaccess.sql.MySqlAuthDao;
import dataaccess.sql.MySqlGameDao;
import dataaccess.sql.MySqlUserDao;
import service.ChessService;
import service.requests.*;
import service.results.*;
import spark.*;
import spark.Request;
import spark.Response;
import com.google.gson.Gson;
import java.util.Objects;

public class Server {

    // Memory Dao implementation
    // ChessService CHESS_SERVICE = new ChessService(new MemoryAuthDao(), new MemoryGameDao(), new MemoryUserDao());

    // Sql Dao implementation
    static ChessService chessService;

    static {
        try {
            chessService = new ChessService(new MySqlAuthDao(), new MySqlGameDao(), new MySqlUserDao());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", Server::clearHandler);
        Spark.post("/user", Server::registerHandler);
        Spark.post("/session", Server::loginHandler);
        Spark.delete("/session", Server::logoutHandler);
        Spark.get("/game", Server::listGamesHandler);
        Spark.post("/game", Server::createGameHandler);
        Spark.put("/game", Server::joinGameHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private static String authorizationHelper(Response res, DataAccessException e) {
        if (Objects.equals(e.getMessage(), "Error: unauthorized")) {
            res.status(401);
        } else {
            res.status(500);
        }
        return "{\"message\": \"" + e.getMessage() + "\"}";
    }

    private static Object clearHandler(Request req, Response res) throws DataAccessException {
        Gson gson = new Gson();

        ClearResult result = chessService.clear();

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object registerHandler(Request req, Response res) {
        Gson gson = new Gson();
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);
            RegisterResult result = chessService.register(request);

            res.type("application/json");
            return gson.toJson(result);
        } catch (DataAccessException e) {
            if (Objects.equals(e.getMessage(), "Error: bad request")) {
                res.status(400);
            } else if (Objects.equals(e.getMessage(), "Error: already taken")){
                res.status(403);
            } else {
                res.status(500);
            }
            return "{\"message\": \"" + e.getMessage() + "\"}";
        }
    }

    private static Object loginHandler(Request req, Response res) {
        Gson gson = new Gson();
        try {
            LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);

            LoginResult result = chessService.login(request);

            res.type("application/json");
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return authorizationHelper(res, e);
        }
    }

    private static Object logoutHandler(Request req, Response res) {
        Gson gson = new Gson();
        try {
            String authToken = req.headers("authorization");
            LogoutRequest request = new LogoutRequest(authToken);
            LogoutResult result = chessService.logout(request);
            res.type("application/json");
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return authorizationHelper(res, e);
        }
    }

    private static Object listGamesHandler(Request req, Response res) {
        Gson gson = new Gson();
        try {
            String authToken = req.headers("authorization");
            ListGamesRequest request = new ListGamesRequest(authToken);
            ListGamesResult result = chessService.listGames(request);

            res.type("application/json");
            return gson.toJson(result);
        } catch (DataAccessException e) {
            return authorizationHelper(res, e);
        }
    }

    private static Object createGameHandler(Request req, Response res) {
        Gson gson = new Gson();
        try {
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
            String gameName = jsonObject.get("gameName").getAsString();
            String authToken = req.headers("authorization");

            CreateGameRequest request = new CreateGameRequest(gameName, authToken);
            CreateGameResult result = chessService.createGame(request);

            res.type("application/json");
            return gson.toJson(result);
        } catch (DataAccessException e) {
            if (Objects.equals(e.getMessage(), "Error: bad request")) {
                res.status(400);
            } else if (Objects.equals(e.getMessage(), "Error: unauthorized")){
                res.status(401);
            } else {
                res.status(500);
            }
            return "{\"message\": \"" + e.getMessage() + "\"}";
        }
    }

    private static Object joinGameHandler(Request req, Response res) {
        Gson gson = new Gson();
        try {
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
            if (!jsonObject.has("playerColor") || jsonObject.get("playerColor").isJsonNull() ||
            !jsonObject.has("gameID") || jsonObject.get("gameID").isJsonNull()) {
                res.status(400);
                return "{\"message\": \"Error: bad request" + "\"}";
            }
            ChessGame.TeamColor playerColor = ChessGame.TeamColor.valueOf(jsonObject.get("playerColor").getAsString().toUpperCase());
            int gameID = jsonObject.get("gameID").getAsInt();
            String authToken = req.headers("authorization");
            JoinGameRequest request = new JoinGameRequest(playerColor, gameID, authToken);

            JoinGameResult result = chessService.joinGame(request);

            res.type("application/json");
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            res.status(400);
            return "{\"message\": \"" + "Error: bad request" + "\"}";
        } catch (DataAccessException e) {
            if (Objects.equals(e.getMessage(), "Error: bad request")) {
                res.status(400);
            } else if (Objects.equals(e.getMessage(), "Error: unauthorized")) {
                res.status(401);
            } else if (Objects.equals(e.getMessage(), "Error: already taken")) {
                res.status(403);
            } else {
                res.status(500);
            }
            return "{\"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
