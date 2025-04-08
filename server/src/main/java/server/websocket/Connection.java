package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String authToken;
    public int gameId;
    public Session session;

    public Connection(String authToken, int gameId, Session session) {
        this.authToken = authToken;
        this.gameId = gameId;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}