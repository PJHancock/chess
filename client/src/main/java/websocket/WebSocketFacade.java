package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.sql.MySqlGameDao;
import model.GameData;
import ui.DataAccessException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

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
                    ServerMessage notification = gson.fromJson(message, ServerMessage.class);
                    commandHandler.notify(notification);
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
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
            this.session.getBasicRemote().sendText(gson.toJson(action));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeMove(String authToken, int gameId, ChessMove move) throws DataAccessException {
        try {
            MySqlGameDao mySqlGameDao = new MySqlGameDao();
            GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));
            if (gameData.game().gameOver) {
                return;
            }
            var action = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, move);
            this.session.getBasicRemote().sendText(gson.toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage());
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void leaveGame(String authToken, int gameId) throws DataAccessException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId);
            this.session.getBasicRemote().sendText(gson.toJson(action));
            this.session.close();
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    public void resign(String authToken, int gameId) throws DataAccessException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId);
            this.session.getBasicRemote().sendText(gson.toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

}