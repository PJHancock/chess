package client;

import chess.*;
import model.GameData;
import ui.DataAccessException;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static ui.EscapeSequences.*;
import static ui.Repl.SCANNER;

public class GameplayClient {
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;

    public GameplayClient(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public void connectWebsocket(String authToken, int gameId) throws DataAccessException {
        ws = new WebSocketFacade(serverUrl, notificationHandler);
        ws.connectToGame(authToken, gameId);
    }

    public void disconnectWebsocket() throws IOException {
        if (ws != null && ws.session.isOpen()) {
            ws.session.close();
        }
    }

    public String eval(String input, String authToken, int gameId, String teamColor) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw(authToken, gameId);
                case "move" -> move(authToken, gameId, teamColor, params);
                case "highlight" -> highlight(authToken, gameId, params);
                case "leave" -> leave(authToken, gameId, teamColor);
                case "resign" -> resign(authToken, gameId, teamColor);
                default -> help();
            };
        } catch (DataAccessException ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
        }
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + "redraw " +
                RESET_TEXT_COLOR + "- chess board\n" +
                SET_TEXT_COLOR_BLUE + "move <a1> <b2> [promotion piece type]" +
                RESET_TEXT_COLOR + "- piece from position a1 to b2. Promote pawn to new piece if applicable\n" +
                SET_TEXT_COLOR_BLUE + "highlight <a1> " +
                RESET_TEXT_COLOR + "- possible moves from piece at location a1\n" +
                SET_TEXT_COLOR_BLUE + "leave " +
                RESET_TEXT_COLOR + "- the game\n" +
                SET_TEXT_COLOR_BLUE + "resign " +
                RESET_TEXT_COLOR + "- if you want to forfeit\n" +
                SET_TEXT_COLOR_BLUE + "help " +
                RESET_TEXT_COLOR + "- with possible commands" +
                "\n" + RESET_TEXT_COLOR + "[Gameplay] >>> " + SET_TEXT_COLOR_GREEN;
    }

    private String resign(String authToken, int gameId, String teamColor) throws DataAccessException {
        if (teamColor == null) {
            throw new DataAccessException("You are an observer and can't resign" +
                    "\n" + RESET_TEXT_COLOR + "[Gameplay] >>> " + SET_TEXT_COLOR_GREEN);
        }
        System.out.print("Do you want to resign? (Y)es/(N)o: ");
        String confirmation = SCANNER.nextLine();

        while (true) {
            if (confirmation.equalsIgnoreCase("y")) {
                ws.resign(authToken, gameId);
                return "You resigned";
            } else if (confirmation.equalsIgnoreCase("n")) {
                return "You did not resign" + "\n" + RESET_TEXT_COLOR + "[Gameplay] >>> " + SET_TEXT_COLOR_GREEN;
            } else {
                System.out.print("Invalid input. Please enter Y or N: ");
                confirmation = SCANNER.nextLine();
            }
        }
    }

    private String leave(String authToken, int gameId, String teamColor) throws DataAccessException {
        // Remove player from game
        if (teamColor == null) {
            return "You stopped watching the game";
        } else if (teamColor.equalsIgnoreCase("white")) {
            ws.leaveGame(authToken, gameId);
        } else if (teamColor.equalsIgnoreCase("black")){
            ws.leaveGame(authToken, gameId);
        }
        return "You left the game";
    }

    public String highlightedBoard(GameData gameData, String teamColor, ChessPosition piecePosition) {

        Collection<ChessMove> possibleMoves = gameData.game().validMoves(piecePosition);
        if (possibleMoves == null) {
            return "No piece located at that position";
        } else if (possibleMoves.isEmpty()){
            return "No valid moves from piece located at that position";
        } else {
            // Return the modified board with highlights
            return drawBoard(gameData, teamColor, possibleMoves);
        }
    }

    public String highlight(String authToken, int gameId, String... params) throws DataAccessException {
        try {
            ChessPosition piecePosition;
            //  Validate the input position (e.g., 'a1', 'h8')
            if ((params.length != 1) || (params[0].length() != 2)) {
                throw new DataAccessException("Expected: <a1>");
            }
            char col = params[0].charAt(0);
            char row = params[0].charAt(1);
            // Ensure the position is within the valid chess range
            if ((col >= 'a' && col <= 'h') && (row >= '1' && row <= '8')) {
                // Convert the column and row to board coordinates (1-indexed)
                int colNum = col - 'a' + 1;  // 'a' → 1, 'h' → 8
                int rowNum = Character.getNumericValue(row);  // '1' → 1, '8' → 8

                // Create a ChessPosition for the selected piece
                piecePosition = new ChessPosition(rowNum, colNum);
                ws.highlight(authToken, gameId, piecePosition);
                return "";
            } else {
                throw new DataAccessException("Expected: <a1>");
            }
        } catch (DataAccessException e) {
            throw new DataAccessException(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
        }
    }

    public String redraw(String authToken, int gameId) throws DataAccessException {
        ws.redraw(authToken, gameId);
        return "";
    }

    public String redrawBoard(GameData gameData, String teamColor) {
        return drawBoard(gameData, teamColor, null);
    }

    private String move(String authToken, int gameId, String teamColor, String... params) throws DataAccessException {
        try {
            // Validate that it is your turn
            if (teamColor == null) {
                throw new DataAccessException("You are watching, not playing the game!" +
                        "\n" + RESET_TEXT_COLOR + "[Gameplay] >>> " + SET_TEXT_COLOR_GREEN);
            }
            ChessPiece.PieceType promotionPiece = null;
            if ((params.length < 2 || params.length > 3) || (params[0].length() != 2) || params[1].length() != 2) {
                throw new DataAccessException("Expected: <a1> <b2> [promotion piece type]");
            }
            if (params.length == 3) {
                if (Arrays.stream(ChessPiece.PieceType.values())
                        .noneMatch(c -> c.name().equalsIgnoreCase(params[2]))) {
                    throw new DataAccessException("Not valid promotion piece");
                }
                promotionPiece = ChessPiece.PieceType.valueOf(params[2].toUpperCase());
            }
            char colStart = params[0].charAt(0);
            char rowStart = params[0].charAt(1);
            char colEnd = params[1].charAt(0);
            char rowEnd = params[1].charAt(1);
            // Ensure the position is within the valid chess range
            if ((colStart >= 'a' && colStart <= 'h') && (rowStart >= '1' && rowStart <= '8') &&
                    (colEnd >= 'a' && colEnd <= 'h') && (rowEnd >= '1' && rowEnd <= '8')) {
                // Convert the column and row to board coordinates (1-indexed)
                int colNumStart = colStart - 'a' + 1;  // 'a' → 1, 'h' → 8
                int rowNumStart = Character.getNumericValue(rowStart);  // '1' → 1, '8' → 8
                int colNumEnd = colEnd - 'a' + 1;  // 'a' → 1, 'h' → 8
                int rowNumEnd = Character.getNumericValue(rowEnd);  // '1' → 1, '8' → 8

                // Create a ChessPosition for the selected piece
                ChessPosition pieceStartPosition = new ChessPosition(rowNumStart, colNumStart);
                ChessPosition pieceEndPosition = new ChessPosition(rowNumEnd, colNumEnd);
                ChessMove desiredMove = new ChessMove(pieceStartPosition, pieceEndPosition, promotionPiece);
                ws.makeMove(authToken, gameId, desiredMove);
                return ""; //String.format(SET_TEXT_COLOR_GREEN + "You moved piece from %c%d to %c%d" + RESET_TEXT_COLOR,
                        // startCol, startRow, endCol, endRow); // String.format("Made move from " + pieceStartPosition + " to " + pieceEndPosition);
            } else {
                throw new DataAccessException("Expected: <a1> <b2>");
            }
        } catch (DataAccessException e) {
            throw new DataAccessException(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
        }
    }

    public String drawBoard(GameData gameData, String teamColor, Collection<ChessMove> possibleMoves) {
        ChessBoard board = gameData.game().getBoard();
        StringBuilder boardString = new StringBuilder();
        Collection<ChessPosition> endPositions = new ArrayList<>();
        ChessPosition startPosition = new ChessPosition(0,0);
        if (possibleMoves != null) {
            for (ChessMove move : possibleMoves) {
                startPosition = move.getStartPosition();
                endPositions.add(move.getEndPosition());
            }
        }

        // Place top columns
        if (teamColor == null || teamColor.equals("white")) {
            boardString.append(getWhiteColumns()).append("\n");
        } else {
            boardString.append(getBlackColumns()).append("\n");
        }
        for (int i = 1; i <= 8; i++) {
            if (teamColor == null || teamColor.equals("white")) {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(9-i).append(" " + RESET_TEXT_COLOR + RESET_BG_COLOR);
            } else {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(i).append(" " + RESET_TEXT_COLOR + RESET_BG_COLOR);
            }
            for (int j = 1; j <= 8; j++) {
                ChessPosition currentPosition = new ChessPosition(i,9-j);
                if (teamColor == null || teamColor.equals("white")) {
                    currentPosition = new ChessPosition(9-i,j);
                }
                if (startPosition.equals(currentPosition)) {
                    ChessPiece piece = board.getPiece(currentPosition);
                    boardString.append(SET_BG_COLOR_YELLOW);
                    boardString.append(" ").append(getPiece(piece, true)).append(" ");
                } else if (endPositions.contains(currentPosition)) {
                    ChessPiece piece = board.getPiece(currentPosition);
                    if ((i + j) % 2 == 0) {
                        boardString.append(SET_BG_COLOR_GREEN);
                    } else {
                        boardString.append(SET_BG_COLOR_DARK_GREEN);
                    }
                    boardString.append(" ").append(getPiece(piece, true)).append(" ");
                } else {
                    ChessPiece piece = board.getPiece(currentPosition);
                    if ((i + j) % 2 == 0) {
                        boardString.append(SET_BG_COLOR_LIGHT_GREY);
                    } else {
                        boardString.append(SET_BG_COLOR_BLACK);
                    }
                    boardString.append(" ").append(getPiece(piece, false)).append(" ");
                }
            }
            if (teamColor == null || teamColor.equals("white")) {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(9-i).append(" " + RESET_BG_COLOR + "\n");
            } else {
                boardString.append(SET_BG_COLOR_WHITE + " ").append(SET_TEXT_COLOR_BLACK).append(i).append(" " + RESET_BG_COLOR + "\n");
            }
            }
        // Place bottom columns
        if (teamColor == null || teamColor.equals("white")) {
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
                "   " + RESET_BG_COLOR + RESET_TEXT_COLOR;
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
                "   " + RESET_BG_COLOR + RESET_TEXT_COLOR;
    }

    private String getPiece(ChessPiece piece, boolean highlight) {
        StringBuilder pieceString = new StringBuilder();
        if (piece == null) {
            return EMPTY;
        }
        ChessPiece.PieceType pieceType = piece.getPieceType();
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        if (teamColor.equals(ChessGame.TeamColor.WHITE)) {
            if (highlight) {
                pieceString.append(SET_TEXT_COLOR_BLACK);
            } else {
                pieceString.append(SET_TEXT_COLOR_RED);
            }
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
        } else {
            if (highlight) {
                pieceString.append(SET_TEXT_COLOR_BLACK);
            } else {
                pieceString.append(SET_TEXT_COLOR_BLUE);
            }
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
        }
        pieceString.append(RESET_TEXT_COLOR);
        return pieceString.toString();
    }
}
