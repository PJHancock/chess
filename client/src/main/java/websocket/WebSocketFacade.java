package websocket;

import com.google.gson.Gson;
import ui.DataAccessException;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    public Session session;
    public NotificationHandler commandHandler;
    private static final Gson gson = new Gson();


    public WebSocketFacade(String url, NotificationHandler commandHandler) throws DataAccessException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.commandHandler = commandHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                public void onMessage(String message) {
                    UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
                    commandHandler.notify(command);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connectToGame(String authToken, int gameId) throws DataAccessException {
        try {
            String message = "Connected to game " + gameId;
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId, message);
            this.session.getBasicRemote().sendText(gson.toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    public void makeMove(String authToken, int gameId) throws DataAccessException {
        try {
            String message = "Made a move";
            var action = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, message);
            this.session.getBasicRemote().sendText(gson.toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    public void leaveGame(String authToken, int gameId) throws DataAccessException {
        try {
            String message = "left the game";
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId, message);
            this.session.getBasicRemote().sendText(gson.toJson(action));
            if (this.session.isOpen()) {
                this.session.close();
            }
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    public void resign(String authToken, int gameId) throws DataAccessException {
        try {
            String message = "resigned the game";
            var action = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId, message);
            this.session.getBasicRemote().sendText(gson.toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

}