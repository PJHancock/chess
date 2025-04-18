package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.sql.MySqlAuthDao;
import dataaccess.sql.MySqlGameDao;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;
import spark.Spark;

import java.io.IOException;
import java.util.Collection;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    public static void main(String[] args) {
        Spark.port(8080);
        Spark.webSocket("/ws", WebSocketHandler.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException, InvalidMoveException {
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
            case MAKE_MOVE -> makeMove(action.getAuthToken(), action.getGameID(), action.getMove(), session);
            case RESIGN -> resign(action.getAuthToken(), action.getGameID(), session);
            case REDRAW_GAME_BOARD -> redrawGameBoard(action.getGameID(), session);
            case HIGHLIGHT_GAME_BOARD -> highlightGameBoard(action.getGameID(), action.getPiecePosition(), session);
            default -> {
                // If the command type is unknown, send an error message
                String errorMessage = "Error: Invalid command type";
                ServerMessage errorNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                session.getRemote().sendString(errorNotification.toString());
            }
        }
    }

    private void connect(String authToken, int gameId, Session session) throws IOException {
        try {
            // Add the root client to the connection manager

            connections.add(authToken, gameId, session);

            // Retrieve the username for the root client
            MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
            String username = mySqlAuthDao.getUser(authToken);
            MySqlGameDao mySqlGameDao = new MySqlGameDao();
            GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));
            if (username == null) {
                throw new DataAccessException("Error: invalid authToken");
            } else if (gameData == null) {
                throw new DataAccessException("Error: invalid gameId");
            }
            // Create a message for the root client (LOAD_GAME)
            var loadGameNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);

            // Send LOAD_GAME to the root client
            session.getRemote().sendString(loadGameNotification.toString());

            // Get username color
            String userColor = "an observer";
            if (username.equals(gameData.whiteUsername())) {
                userColor = "white";
            } else if (username.equals(gameData.blackUsername())) {
                userColor = "black";
            }

            // Now broadcast a notification to all other connected clients in the game
            var notificationMessage = String.format("User %s has connected to the game as %s", username, userColor);
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
        MySqlGameDao mySqlGameDao = new MySqlGameDao();
        GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));

        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            mySqlGameDao.updateGameUsername(null, ChessGame.TeamColor.WHITE, gameId);
        } else if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            mySqlGameDao.updateGameUsername(null, ChessGame.TeamColor.BLACK, gameId);
        }

        String leaveMessage = String.format("%s left the game", username);
        ServerMessage leaveNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, leaveMessage);
        connections.broadcast(authToken, gameId, leaveNotification);
        session.close();
    }

    public void makeMove(String authToken, int gameId, ChessMove move, Session session)
            throws IOException, DataAccessException, InvalidMoveException {
        MySqlGameDao mySqlGameDao = new MySqlGameDao();
        GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);

        if (username == null) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid auth")));
            return;
        }

        boolean isWhite = username.equals(gameData.whiteUsername());
        boolean isBlack = username.equals(gameData.blackUsername());

        if (!isWhite && !isBlack) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You are an observer")));
            return;
        }

        if (gameData.game().gameOver) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "No more moves can be made. Game is over")));
            return;
        }

        // Check turn
        ChessGame.TeamColor currentTurn = gameData.game().getTeamTurn();
        if ((currentTurn == ChessGame.TeamColor.WHITE && !isWhite) ||
                (currentTurn == ChessGame.TeamColor.BLACK && !isBlack)) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "It is not your turn")));
            return;
        }

        // Validate piece ownership
        ChessPiece piece = gameData.game().getBoard().getPiece(move.getStartPosition());
        if (piece == null) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "There is no piece on that square")));
            return;
        }

        if ((isWhite && piece.getTeamColor() != ChessGame.TeamColor.WHITE) ||
                (isBlack && piece.getTeamColor() != ChessGame.TeamColor.BLACK)) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "That is not your piece")));
            return;
        }

        // Validate move legality
        Collection<ChessMove> validMoves = gameData.game().validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            session.getRemote().sendString(new Gson().toJson(
                    new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid move")));
            return;
        }

        gameData.game().makeMove(move);
        mySqlGameDao.updateGameBoard(gameData.game(), gameId);
        // Send LOAD_GAME message to all clients
        var loadGameNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        session.getRemote().sendString(loadGameNotification.toString());
        connections.broadcast(authToken, gameId, loadGameNotification);
        char startCol = (char) ('a' + move.getStartPosition().getColumn() - 1);
        int startRow = move.getStartPosition().getRow();
        char endCol = (char) ('a' + move.getEndPosition().getColumn() - 1);
        int endRow = move.getEndPosition().getRow();
        String moveMessage = String.format("User %s moved piece from %c%d to %c%d",
                username, startCol, startRow, endCol, endRow);
        ServerMessage moveNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage);
        connections.broadcast(authToken, gameId, moveNotification);

        String userInTrouble = gameData.whiteUsername();
        if (gameData.game().getTeamTurn() == ChessGame.TeamColor.WHITE) {
            userInTrouble = gameData.whiteUsername();
        } else if (gameData.game().getTeamTurn() == ChessGame.TeamColor.BLACK) {
            userInTrouble = gameData.blackUsername();
        }
        // Check for check, checkmate, or stalemate
        if (gameData.game().isInCheckmate(gameData.game().getTeamTurn())) {
            String checkmateMessage = String.format("\n%s is in checkmate. Game over", userInTrouble);
            ServerMessage checkmateNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, checkmateMessage);
            connections.broadcast("", gameId, checkmateNotification);
            // Mark game as over
            gameData.game().setGameOver(true);
            mySqlGameDao.updateGameBoard(gameData.game(), gameId);
        } else if (gameData.game().isInStalemate(gameData.game().getTeamTurn())) {
            String stalemateMessage = String.format("\n%s is in stalemate. Game over", userInTrouble);
            ServerMessage stalemateNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, stalemateMessage);
            connections.broadcast("", gameId, stalemateNotification);
            // Mark game as over
            gameData.game().setGameOver(true);
            mySqlGameDao.updateGameBoard(gameData.game(), gameId);
        } else if (gameData.game().isInCheck(gameData.game().getTeamTurn())) {
            String checkMessage = String.format("\n%s is in check", userInTrouble);
            ServerMessage checkNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, checkMessage);
            connections.broadcast("", gameId, checkNotification);
        }
    }

    public void resign(String authToken, int gameId, Session session) throws DataAccessException, IOException {
        MySqlAuthDao mySqlAuthDao = new MySqlAuthDao();
        String username = mySqlAuthDao.getUser(authToken);
        MySqlGameDao mySqlGameDao = new MySqlGameDao();
        GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));

        if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
            session.getRemote().sendString(new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR, "You are an observer")));
            return;
        } else if (gameData.game().gameOver) {
            session.getRemote().sendString(new Gson().toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR,
                    "Can't resign. Game is already over")));
            return;
        }

        String resignMessage = String.format("%s resigned from the game", username);
        gameData.game().setGameOver(true); // mark game as over
        mySqlGameDao.updateGameBoard(gameData.game(), gameId);

        ServerMessage resignNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, resignMessage);
        connections.broadcast("", gameId, resignNotification);
    }

    public void redrawGameBoard(int gameId, Session session) throws DataAccessException, IOException {
        MySqlGameDao mySqlGameDao = new MySqlGameDao();
        GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));
        mySqlGameDao.updateGameBoard(gameData.game(), gameId);
        // Send LOAD_GAME message to all clients
        var redrawGameNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        session.getRemote().sendString(redrawGameNotification.toString());
    }

    public void highlightGameBoard(int gameId, ChessPosition piecePosition, Session session) throws DataAccessException, IOException {
        MySqlGameDao mySqlGameDao = new MySqlGameDao();
        GameData gameData = mySqlGameDao.getGameUsingId(String.valueOf(gameId));
        mySqlGameDao.updateGameBoard(gameData.game(), gameId);
        // Send LOAD_GAME message to all clients
        var highlightGameNotification = new ServerMessage(ServerMessage.ServerMessageType.HIGHLIGHT_GAME, gameData, piecePosition);
        session.getRemote().sendString(highlightGameNotification.toString());
    }
}