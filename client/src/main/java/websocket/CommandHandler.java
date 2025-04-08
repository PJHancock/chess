package websocket;

import websocket.commands.UserGameCommand;

public interface CommandHandler {
    static void notify(UserGameCommand.CommandType notification) {

    }
}
