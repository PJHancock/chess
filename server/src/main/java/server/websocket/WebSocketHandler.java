package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.sql.MySqlAuthDao;
import dataaccess.sql.MySqlGameDao;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;
import spark.Spark;

import java.io.IOException;
import java.util.Collection;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    public static void main(String[] args) {
        Spark.port(8080);
        Spark.webSocket("/ws", WebSocketHandler.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand action;
        try {
            action = new Gson().fromJson(message, UserGameCommand.class);
        } catch (Exception e) {
            // If there's an error in parsing or the command is invalid, send an error message
            String errorMessage = "Error: Invalid command format or unknown command";
            ServerMessage errorNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            session.getRemote().sendString(errorNotification.toString());
            return;
        }
        switch (action.getCommandType()) {
            case CONNECT -> connect(action.getAuthToken(), action.getGameID(), session);
            case LEAVE -> leave(action.getAuthToken(), action.getGameID(), session);
            default -> {
                // If the command type is unknown, send an error message
                String errorMessage = "Error: Invalid command type";
                ServerMessage errorNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                session.getRemote().sendString(errorNotification.toString());
            }
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) throws IOException {
        // Handle WebSocket errors
        String errorMessage = throwable.getMessage();
        if (errorMessage == null) {
            errorMessage = "Unknown WebSocket error occurred";
        }
        // Create and send the error message with the errorMessage field populated
        ServerMessage errorNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
        session.getRemote().sendString(errorNotification.toString());
        session.close();
    }


    private void connect(String authToken, int gameId, Session session) throws IOException, DataAccessException {
        try {
            // Add the root client to the connection manager

            connections.add(authToken, gameId, session);

            // Retrieve the username for the root client
            MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
            String username = mySqlAuthDao.getUser(authToken);
            if (username == null) {
                throw new DataAccessException("Error: invalid authToken");
            }
            // Create a message for the root client (LOAD_GAME)
            MySqlGameDao mySqlGameDao = new MySqlGameDao();
            GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));
            var loadGameNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());

            // Send LOAD_GAME to the root client
            session.getRemote().sendString(loadGameNotification.toString());

            // Now broadcast a notification to all other connected clients in the game
            var notificationMessage = String.format("%s has connected to the game as %s", username, "[insert color]");
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationMessage);

            connections.broadcast(authToken, gameId, notification);
        } catch (IOException | DataAccessException ex) {
            String errorMessage = ex.getMessage();
            if (errorMessage == null) {
                errorMessage = "Unknown WebSocket error occurred";
            }
            // Create and send the error message with the errorMessage field populated
            ServerMessage errorNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            session.getRemote().sendString(errorNotification.toString());
            session.close();
        }
    }

    private void leave(String authToken, int gameId, Session session) throws DataAccessException, IOException {
        connections.remove(authToken);
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);
        String leaveMessage = String.format("%s left the game", username);
        ServerMessage leaveNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, leaveMessage);
        connections.broadcast(authToken, gameId, leaveNotification);
        session.close();
    }

    public void makeMove(String authToken, int gameId, ChessMove move, Session session) throws IOException, DataAccessException {
        MySqlGameDao mySqlGameDao = new MySqlGameDao();
        GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));

        // Validate the move (implement your chess logic here)
        Collection<ChessMove> validMoves = gameData.game().validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            session.getRemote().sendString(new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid move")));
            return;
        }

        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);

        // Send LOAD_GAME message to all clients
        var loadGameNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        session.getRemote().sendString(loadGameNotification.toString());
        connections.broadcast(authToken, gameId, loadGameNotification);

        // Now broadcast a notification to all other connected clients in the game
        String moveMessage = String.format("%s moved piece from %s to %s", username, move.getStartPosition().toString(), move.getEndPosition().toString());
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage);
        connections.broadcast(authToken, gameId, notification);

        // Check for check, checkmate, or stalemate
        if (gameData.game().isInCheckmate(gameData.game().getTeamTurn())) {
            String checkmateMessage = String.format("%s is in checkmate", gameData.game().getTeamTurn());
            ServerMessage checkmateNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, checkmateMessage);
            connections.broadcast(authToken, gameId, checkmateNotification);
        } else if (gameData.game().isInStalemate(gameData.game().getTeamTurn())) {
            String stalemateMessage = String.format("%s is in stalemate", gameData.game().getTeamTurn());
            ServerMessage stalemateNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, stalemateMessage);
            connections.broadcast(authToken, gameId, stalemateNotification);
        } else if (gameData.game().isInCheck(gameData.game().getTeamTurn())) {
            String checkMessage = String.format("%s is in checkmate", gameData.game().getTeamTurn());
            ServerMessage checkNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, checkMessage);
            connections.broadcast(authToken, gameId, checkNotification);
        }
    }

    public void resign(String authToken, int gameId, Session session) throws DataAccessException, IOException {
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);
        String resignMessage = String.format("%s resigned from the game", username);
        ServerMessage resignNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, resignMessage);
        connections.broadcast(authToken, gameId, resignNotification);
    }
}