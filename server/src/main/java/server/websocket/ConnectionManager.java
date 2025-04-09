package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Session, String> sessionAuthTokenMap = new ConcurrentHashMap<>();

    public void add(String authToken, int gameId, Session session) {
        var connection = new Connection(authToken, gameId, session);
        connections.put(authToken, connection);
    }

    public void addAuth(Session session, String authToken) {
        sessionAuthTokenMap.put(session, authToken);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void removeAuth(Session session) {
        sessionAuthTokenMap.remove(session);
    }

    public String getAuthToken(Session session) {
        return sessionAuthTokenMap.get(session);
    }

    public void broadcast(String excludeAuthToken, int gameId, ServerMessage notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.gameId == gameId && !c.authToken.equals(excludeAuthToken)) {
                    c.send(notification.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }
}