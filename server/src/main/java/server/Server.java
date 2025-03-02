package server;

import chess.ChessGame;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDao;
import dataaccess.memory.MemoryGameDao;
import dataaccess.memory.MemoryUserDao;
import service.ChessService;
import service.requests.*;
import service.results.*;
import spark.*;
import spark.Request;
import spark.Response;
import com.google.gson.Gson;
import java.util.Objects;

public class Server {
    private static final ChessService CHESS_SERVICE = new ChessService(new MemoryAuthDao(), new MemoryGameDao(), new MemoryUserDao());

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

    private static Object clearHandler(Request req, Response res) {
        Gson gson = new Gson();

        ClearResult result = CHESS_SERVICE.clear();

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object registerHandler(Request req, Response res) {
        Gson gson = new Gson();
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);
            RegisterResult result = CHESS_SERVICE.register(request);

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

            LoginResult result = CHESS_SERVICE.login(request);

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
            LogoutResult result = CHESS_SERVICE.logout(request);
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
            ListGamesResult result = CHESS_SERVICE.listGames(request);

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
            CreateGameResult result = CHESS_SERVICE.createGame(request);

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

            JoinGameResult result = CHESS_SERVICE.joinGame(request);

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
