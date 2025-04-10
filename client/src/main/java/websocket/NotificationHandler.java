package websocket;

import ui.DataAccessException;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage notification) throws DataAccessException;
}
