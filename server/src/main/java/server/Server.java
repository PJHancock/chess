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

    private static Object login(Request req, Response res) {
        Gson gson = new Gson();
        LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);

        UserService service = new UserService();
        LoginResult result = service.login(request);

        res.type("application/json");
        return gson.toJson(result);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
