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
    private MySqlAuthDao authDao;

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
        String username = authDao.getUser(authToken);
        var message = String.format("%s joined the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(username, notification);
    }

    private void leave(String authToken, int gameId) throws DataAccessException, IOException {
        String username = authDao.getUser(authToken);
        var message = String.format("%s left the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, notification);
    }

    public void makeMove(String authToken, String startPosition, String endPosition, Session session) throws IOException, DataAccessException {
        try {
            connections.remove(authToken);
            String username = authDao.getUser(authToken);
            var message = String.format("%s moved piece from %s to %s", username, startPosition, endPosition);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", notification);
        } catch (DataAccessException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    public void resign(String authToken, int gameId, Session session) throws DataAccessException, IOException {
        String username = authDao.getUser(authToken);
        var message = String.format("%s resigned from the game", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, notification);
    }
}
