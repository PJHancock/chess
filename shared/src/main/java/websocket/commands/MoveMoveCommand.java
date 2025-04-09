package websocket.commands;

public class MoveMoveCommand extends UserGameCommand {
    public MoveMoveCommand(CommandType commandType, String authToken, Integer gameID, String message) {
        super(commandType, authToken, gameID, message);
    }
}
