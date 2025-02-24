package server;

import service.requests.*;
import service.results.*;
import service.*;
import spark.*;
import spark.Request;
import spark.Response;
import com.google.gson.Gson;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", Server::clear);
        Spark.post("/user", Server::register);
        Spark.post("/session", Server::login);
        Spark.delete("/session", Server::logout);
        Spark.get("/game", Server::listGames);
        Spark.post("/game", Server::createGame);
        Spark.put("/game", Server::joinGame);

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private static Object clear(Request req, Response res) {
        Gson gson = new Gson();
        ClearRequest request = gson.fromJson(req.body(), ClearRequest.class);

        ClearService service = new ClearService();
        ClearResult result = service.clear(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object register(Request req, Response res) {
        Gson gson = new Gson();
        RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

        UserService service = new UserService();
        RegisterResult result = service.register(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object login(Request req, Response res) {
        Gson gson = new Gson();
        LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);

        UserService service = new UserService();
        LoginResult result = service.login(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object logout(Request req, Response res) {
        Gson gson = new Gson();
        LogoutRequest request = gson.fromJson(req.body(), LogoutRequest.class);

        UserService service = new UserService();
        LogoutResult result = service.logout(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object listGames(Request req, Response res) {
        Gson gson = new Gson();
        ListGamesRequest request = gson.fromJson(req.body(), ListGamesRequest.class);

        GameService service = new GameService();
        ListGamesResult result = service.listGames(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object createGame(Request req, Response res) {
        Gson gson = new Gson();
        CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

        GameService service = new GameService();
        CreateGameResult result = service.createGame(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    private static Object joinGame(Request req, Response res) {
        Gson gson = new Gson();
        JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

        GameService service = new GameService();
        JoinGameResult result = service.joinGame(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
