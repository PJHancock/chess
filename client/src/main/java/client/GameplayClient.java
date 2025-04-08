package client;

import chess.*;
import dataaccess.sql.MySqlGameDao;
import model.GameData;
import ui.DataAccessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static ui.EscapeSequences.*;
import static ui.Repl.SCANNER;

public class GameplayClient {
    private final ServerFacade server;
    // private final String serverUrl;
    private final MySqlGameDao gameDao = new MySqlGameDao();

    public GameplayClient(String serverUrl) throws dataaccess.DataAccessException {
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
                case "highlight" -> highlight(gameData, teamColor, params);
                case "leave" -> leave(gameData, teamColor);
                case "resign" -> resign();
                default -> help();
            };
        } catch (DataAccessException | dataaccess.DataAccessException ex) {
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
                RESET_TEXT_COLOR + "- with possible commands";
    }

    private String resign() {
        System.out.print("Do you want to resign? (Y)es/(N)o: ");
        String confirmation = SCANNER.nextLine();

        while (true) {
            if (confirmation.equalsIgnoreCase("y")) {
                return "You resigned";
            } else if (confirmation.equalsIgnoreCase("n")) {
                return "You did not resign";
            } else {
                System.out.print("Invalid input. Please enter Y or N: ");
                confirmation = SCANNER.nextLine();
            }
        }
    }

    private String leave(GameData gameData, String teamColor) throws dataaccess.DataAccessException {
        // Remove player from game
        if (teamColor == null) {
            return "You stopped watching the game";
        } else if (teamColor.equalsIgnoreCase("white")) {
            gameDao.updateGameUsername(null, ChessGame.TeamColor.WHITE, gameData.gameID());
        } else if (teamColor.equalsIgnoreCase("black")){
            gameDao.updateGameUsername(null, ChessGame.TeamColor.BLACK, gameData.gameID());
        }
        return "You left the game";
    }

    private String highlight(GameData gameData, String teamColor, String... params) throws DataAccessException {
        try {
            Collection<ChessMove> possibleMoves;
            // Validate the input position (e.g., 'a1', 'h8')
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
                ChessPosition piecePosition = new ChessPosition(rowNum, colNum);

                // Get all the valid moves for the piece
                possibleMoves = gameData.game().validMoves(piecePosition);
                if (possibleMoves == null) {
                    throw new DataAccessException("No piece located at " + col + row);
                } else if (possibleMoves.isEmpty()){
                    throw new DataAccessException("No valid moves from piece located at " + col + row);
                } else {
                    // Return the modified board with highlights
                    return drawBoard(gameData, teamColor, possibleMoves);
                }
            } else {
                throw new DataAccessException("Expected: <a1>");
            }
        } catch (DataAccessException e) {
            throw new DataAccessException(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
        }
    }

    public String redraw(GameData gameData, String teamColor) {
        return drawBoard(gameData, teamColor, null);
    }

    private String move(GameData gameData, String teamColor, String... params) throws DataAccessException {
        try {
            // Validate that it is your turn
            if (teamColor == null) {
                throw new DataAccessException("You are watching, not playing the game!");
            }
            if (teamColor.equals("white")) {
                if (gameData.game().getTeamTurn() != ChessGame.TeamColor.WHITE) {
                    throw new DataAccessException("It is not your turn!");
                }
            } else if (gameData.game().getTeamTurn() != ChessGame.TeamColor.BLACK) {
                    throw new DataAccessException("It is not your turn!");
            }
            // Validate the input position (e.g., 'a1', 'h8')
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
            char col_start = params[0].charAt(0);
            char row_start = params[0].charAt(1);
            char col_end = params[1].charAt(0);
            char row_end = params[1].charAt(1);
            // Ensure the position is within the valid chess range
            if ((col_start >= 'a' && col_start <= 'h') && (row_start >= '1' && row_start <= '8') &&
                    (col_end >= 'a' && col_end <= 'h') && (row_end >= '1' && row_end <= '8')) {
                // Convert the column and row to board coordinates (1-indexed)
                int colNumStart = col_start - 'a' + 1;  // 'a' → 1, 'h' → 8
                int rowNumStart = Character.getNumericValue(row_start);  // '1' → 1, '8' → 8
                int colNumEnd = col_end - 'a' + 1;  // 'a' → 1, 'h' → 8
                int rowNumEnd = Character.getNumericValue(row_end);  // '1' → 1, '8' → 8

                // Create a ChessPosition for the selected piece
                ChessPosition pieceStartPosition = new ChessPosition(rowNumStart, colNumStart);
                ChessPosition pieceEndPosition = new ChessPosition(rowNumEnd, colNumEnd);
                ChessMove desiredMove = new ChessMove(pieceStartPosition, pieceEndPosition, promotionPiece);
                // Get all the valid moves for the piece
                Collection<ChessMove> possibleMoves = gameData.game().validMoves(pieceStartPosition);
                if (possibleMoves == null) {
                    throw new DataAccessException("That piece has no valid moves");
                }
                if (possibleMoves.contains(desiredMove)) {
                    gameData.game().makeMove(desiredMove);
                    gameDao.updateGameBoard(gameData.game(), gameData.gameID());
                } else {
                    throw new DataAccessException("Not a valid move");
                }
                // return the new board
                return drawBoard(gameData, teamColor, null);
            } else {
                throw new DataAccessException("Expected: <a1> <b2>");
            }
        } catch (DataAccessException | InvalidMoveException | dataaccess.DataAccessException e) {
            throw new DataAccessException(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
        }
    }

    private String drawBoard(GameData gameData, String teamColor, Collection<ChessMove> possibleMoves) {
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
            boardString.append(getWhiteColumns());
        } else {
            boardString.append(getBlackColumns());
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
