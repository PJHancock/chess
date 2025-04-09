package websocket.commands;

import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    CommandType commandType;
    String authToken;
    Integer gameID;
    ChessMove move;
    ChessPosition piecePosition;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessPosition piecePosition) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.piecePosition = piecePosition;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN,
        REDRAW_GAME_BOARD,
        HIGHLIGHT_GAME_BOARD
    }

    public CommandType getCommandType() {
        return this.commandType;
    }

    public ChessPosition getPiecePosition() {
        return piecePosition;
    }

    public String getAuthToken() {
        return this.authToken;
    }

    public Integer getGameID() {
        return this.gameID;
    }

    public ChessMove getMove() {
        return this.move;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}