package websocket;

import websocket.commands.UserGameCommand;

public interface NotificationHandler {
    void notify(UserGameCommand command);
}
