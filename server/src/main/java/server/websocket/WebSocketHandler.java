package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.sql.MySqlAuthDao;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case CONNECT -> connect(action.getAuthToken(), action.getGameID(), session);
            case LEAVE -> leave(action.getAuthToken(), action.getGameID());
        }
    }

    private void connect(String username, int gameId, Session session) throws IOException, DataAccessException {
        connections.add(username, gameId, session);
        var message = String.format("%s joined the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(username, notification);
    }

    private void leave(String username, int gameId) throws DataAccessException, IOException {
        var message = String.format("%s left the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(username, notification);
    }

    public void makeMove(String username, String startPosition, String endPosition, Session session) throws IOException, DataAccessException {
        connections.remove(username);
        var message = String.format("%s moved piece from %s to %s", username, startPosition, endPosition);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
        connections.broadcast("", notification);
    }

    public void resign(String username, int gameId, Session session) throws DataAccessException, IOException {
        var message = String.format("%s resigned from the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(username, notification);
    }
}
