package server;

import chess.ChessGame;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import service.ChessService;
import service.requests.*;
import service.results.*;
import spark.*;
import spark.Request;
import spark.Response;
import com.google.gson.Gson;

import java.io.Reader;

public class Server {
    private static final ChessService service = new ChessService();

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
//        Spark.exception(DataAccessException.class, Server::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

//    private static void exceptionHandler(DataAccessException ex, Request req, Response res) {
//        res.status(ex.StatusCode());
//        res.body(ex.toJson());
//    }

    private static Object clearHandler(Request req, Response res) {
        Gson gson = new Gson();
        ClearRequest request = gson.fromJson(req.body(), ClearRequest.class);

        ClearResult result = service.clear(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object registerHandler(Request req, Response res) throws DataAccessException {
        Gson gson = new Gson();
        RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

        RegisterResult result = service.register(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object loginHandler(Request req, Response res) throws DataAccessException {
        Gson gson = new Gson();
        LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);

        LoginResult result = service.login(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object logoutHandler(Request req, Response res) throws DataAccessException {
        Gson gson = new Gson();
        String authToken = req.headers("authorization");
        LogoutRequest request = new LogoutRequest(authToken);
        LogoutResult result = service.logout(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object listGamesHandler(Request req, Response res) throws DataAccessException {
        Gson gson = new Gson();
        String authToken = req.headers("authorization");
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResult result = service.listGames(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object createGameHandler(Request req, Response res) throws DataAccessException {
        Gson gson = new Gson();

        JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
        String gameName = jsonObject.get("gameName").getAsString();
        String authToken = req.headers("authorization");

        CreateGameRequest request = new CreateGameRequest(gameName, authToken);
        CreateGameResult result = service.createGame(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object joinGameHandler(Request req, Response res) throws DataAccessException {
        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
        ChessGame.TeamColor playerColor = ChessGame.TeamColor.valueOf(jsonObject.get("playerColor").getAsString());
        int gameID = jsonObject.get("gameID").getAsInt();
        String authToken = req.headers("authorization");
        JoinGameRequest request = new JoinGameRequest(playerColor, gameID, authToken);

        JoinGameResult result = service.joinGame(request);

        res.type("application/json");
        return gson.toJson(result);
    }
}
