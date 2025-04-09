package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.sql.MySqlAuthDao;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;
import spark.Spark;

import java.io.IOException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    public static void main(String[] args) {
        Spark.port(8080);
        Spark.webSocket("/ws", WebSocketHandler.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case CONNECT -> connect(action.getAuthToken(), action.getGameID(), session);
            case LEAVE -> leave(action.getAuthToken(), action.getGameID());
        }
    }

    private void connect(String authToken, int gameId, Session session) throws IOException, DataAccessException {
        connections.add(authToken, gameId, session);
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);
        var message = String.format("%s joined the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(authToken, gameId, notification);
    }

    private void leave(String authToken, int gameId) throws DataAccessException, IOException {
        connections.remove(authToken);
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);
        var message = String.format("%s left the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, gameId, notification);
    }

//    @OnWebSocketClose
//    public void onClose(Session session, int statusCode) throws IOException {
//        String authToken = getAuthTokenFromSession(session); // You will need to implement a way to retrieve the authToken
//        int gameId = getGameIdFromSession(session); // Similarly, implement to fetch the gameId
//        leave(authToken, gameId); // Handle clean-up after disconnection
//        System.out.println("Connection closed with status: " + statusCode);
//    }

    // Handles any errors that occur during the WebSocket connection
    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    public void makeMove(String authToken, int gameId, String startPosition, String endPosition, Session session) throws IOException, DataAccessException {
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);
        var message = String.format("%s moved piece from %s to %s", username, startPosition, endPosition);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(authToken, gameId, notification);
    }

    public void resign(String authToken, int gameId, Session session) throws DataAccessException, IOException {
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);
        var message = String.format("%s resigned from the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, gameId, notification);
    }
}
