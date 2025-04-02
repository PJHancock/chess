package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import ui.DataAccessException;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class GameplayClient {
    private final ServerFacade server;
    // private final String serverUrl;

    public GameplayClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        // this.serverUrl = serverUrl;
    }

    public String eval(String input, GameData gameData, String teamColor) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw(gameData, teamColor);
                case "move" -> move(gameData, teamColor, params);
                case "highlight" -> highlight(gameData, params);
                case "leave" -> leave();
                case "resign" -> resign();
                default -> help();
            };
        } catch (DataAccessException ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
        }
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + "redraw " +
                RESET_TEXT_COLOR + "- chess board\n" +
                SET_TEXT_COLOR_BLUE + "move <x1,y1> <x2,y2> " +
                RESET_TEXT_COLOR + "- piece from <x1,y1> to <x2,y2>\n" +
                SET_TEXT_COLOR_BLUE + "highlight <x,y> " +
                RESET_TEXT_COLOR + "- possible moves from piece at <x1,y1> \n" +
                SET_TEXT_COLOR_BLUE + "leave " +
                RESET_TEXT_COLOR + "- the game \n" +
                SET_TEXT_COLOR_BLUE + "resign " +
                RESET_TEXT_COLOR + "- if you want to forfeit \n" +
                SET_TEXT_COLOR_BLUE + "help " +
                RESET_TEXT_COLOR + "- with possible commands";
    }

    private String resign() {
        return "Do you want to resign? (Y)es/(N)o ";
    }

    private String leave() {
        return "You left the game";
    }

    private String highlight(GameData gameData, String[] params) throws DataAccessException {
        return null;
    }

    public String redraw(GameData gameData, String teamColor) {
        return drawBoard(gameData, teamColor);
    }

    private String move(GameData gameData, String teamColor, String... params) throws DataAccessException {
        return null;
    }

    private String drawBoard(GameData gameData, String teamColor) {
        ChessBoard board = gameData.game().getBoard();
        StringBuilder boardString = new StringBuilder();
        // Place top columns
        if (teamColor.equals("white")) {
            boardString.append(getWhiteColumns());
        } else {
            boardString.append(getBlackColumns());
        }
        for (int i = 1; i <= 8; i++) {
            if (teamColor.equals("white")) {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(9-i).append(" " + RESET_TEXT_COLOR + RESET_BG_COLOR);
            } else {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(i).append(" " + RESET_TEXT_COLOR + RESET_BG_COLOR);
            }
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i,j));
                if ((i + j) % 2 == 0) {
                    boardString.append(SET_BG_COLOR_LIGHT_GREY);
                } else {
                    boardString.append(SET_BG_COLOR_BLACK);
                }
                boardString.append(" ").append(getPiece(piece)).append(" ");
            }
            if (teamColor.equals("white")) {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(9-i).append(" " + RESET_BG_COLOR + "\n");
            } else {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(i).append(" " + RESET_BG_COLOR + "\n");
            }
            }
        // Place bottom columns
        if (teamColor.equals("white")) {
            boardString.append(getWhiteColumns());
        } else {
            boardString.append(getBlackColumns());
        }
        return boardString.toString();
    }

    private static String getWhiteColumns() {
        return SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "   " +
                "  " + "a" + "  " +
                "  " + "b" + "  " +
                "  " + "c" + "  " +
                "  " + "d" + "  " +
                "  " + "e" + "  " +
                "  " + "f" + "  " +
                "  " + "g" + "  " +
                "  " + "h" + "  " +
                "   " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";
    }

    private static String getBlackColumns() {
        return SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "   " +
                "  " + "h" + "  " +
                "  " + "g" + "  " +
                "  " + "f" + "  " +
                "  " + "e" + "  " +
                "  " + "d" + "  " +
                "  " + "c" + "  " +
                "  " + "b" + "  " +
                "  " + "a" + "  " +
                "   " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";
    }

    private String getPiece(ChessPiece piece) {
        StringBuilder pieceString = new StringBuilder();
        if (piece == null) {
            return EMPTY;
        }
        ChessPiece.PieceType pieceType = piece.getPieceType();
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        if (teamColor.equals(ChessGame.TeamColor.WHITE)) {
            pieceString.append(SET_TEXT_COLOR_RED);
            if (pieceType.equals(ChessPiece.PieceType.PAWN)) {
                pieceString.append(WHITE_PAWN);
            } else if (pieceType.equals(ChessPiece.PieceType.ROOK)) {
                pieceString.append(WHITE_ROOK);
            } else if (pieceType.equals(ChessPiece.PieceType.BISHOP)) {
                pieceString.append(WHITE_BISHOP);
            } else if (pieceType.equals(ChessPiece.PieceType.KNIGHT)) {
                pieceString.append(WHITE_KNIGHT);
            } else if (pieceType.equals(ChessPiece.PieceType.QUEEN)) {
                pieceString.append(WHITE_QUEEN);
            } else {
                pieceString.append(WHITE_KING);
            }
            pieceString.append(RESET_TEXT_COLOR);
            return pieceString.toString();
        } else {
            pieceString.append(SET_TEXT_COLOR_BLUE);
            if (pieceType.equals(ChessPiece.PieceType.PAWN)) {
                pieceString.append(BLACK_PAWN);
            } else if (pieceType.equals(ChessPiece.PieceType.ROOK)) {
                pieceString.append(BLACK_ROOK);
            } else if (pieceType.equals(ChessPiece.PieceType.BISHOP)) {
                pieceString.append(BLACK_BISHOP);
            } else if (pieceType.equals(ChessPiece.PieceType.KNIGHT)) {
                pieceString.append(BLACK_KNIGHT);
            } else if (pieceType.equals(ChessPiece.PieceType.QUEEN)) {
                pieceString.append(BLACK_QUEEN);
            } else {
                pieceString.append(BLACK_KING);
            }
            pieceString.append(RESET_TEXT_COLOR);
            return pieceString.toString();
        }
    }
}
