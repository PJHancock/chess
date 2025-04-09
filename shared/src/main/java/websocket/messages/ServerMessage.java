package websocket.messages;

import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String errorMessage;
    String message;
    GameData game;
    ChessPosition piecePosition;

    public enum ServerMessageType {
        LOAD_GAME,
        HIGHLIGHT_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        if (type.equals(ServerMessageType.ERROR)) {
            this.errorMessage = message;
        } else {
            this.message = message;
        }
    }

    public ServerMessage(ServerMessageType type, GameData game) {
        this.serverMessageType = type;
        this.game = game;
    }

    public ServerMessage(ServerMessageType type, GameData game, ChessPosition piecePosition) {
        this.serverMessageType = type;
        this.game = game;
        this.piecePosition = piecePosition;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public GameData getGameData() {
        return this.game;
    }

    public String getMessage() {
        return this.message;
    }

    public ChessPosition getPiecePosition() {
        return piecePosition;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}