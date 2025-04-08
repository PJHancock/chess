package websocket;

import websocket.commands.UserGameCommand;

public interface NotificationHandler {
    static void notify(UserGameCommand.CommandType notification) {

    }
}
