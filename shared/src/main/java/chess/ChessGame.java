package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private static TeamColor teamTurn = TeamColor.WHITE;
    public static ChessBoard gameBoard = new ChessBoard();

    public ChessGame() {
        gameBoard.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = gameBoard.getPiece(startPosition);
        if (piece != null) {
            Collection<ChessMove> validMoves = piece.pieceMoves(gameBoard, startPosition);
            Iterator<ChessMove> iterator = validMoves.iterator();
            while (iterator.hasNext()) {
                ChessMove possibleMove = iterator.next();
                ChessPiece opponentPiece = gameBoard.getPiece(possibleMove.getEndPosition());
                gameBoard.addPiece(possibleMove.getStartPosition(), null);
                gameBoard.addPiece(possibleMove.getEndPosition(), piece);
                if (isInCheck(piece.getTeamColor())) {
                    iterator.remove();
                }
                gameBoard.addPiece(possibleMove.getStartPosition(), piece);
                gameBoard.addPiece(possibleMove.getEndPosition(), opponentPiece);
            }
            return validMoves;
        } else {
            return null;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = gameBoard.getPiece(move.getStartPosition());
        if ((piece != null) && (piece.getTeamColor() == getTeamTurn())) {
            if (validMoves(move.getStartPosition()).contains(move)) {
                gameBoard.addPiece(move.getStartPosition(), null);
                if (move.getPromotionPiece() != null) {
                    gameBoard.addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
                } else {
                    gameBoard.addPiece(move.getEndPosition(), piece);
                }
                if (piece.getTeamColor() == TeamColor.WHITE) {
                    setTeamTurn(TeamColor.BLACK);
                } else {
                    setTeamTurn(TeamColor.WHITE);
                }
            } else {
                throw new InvalidMoveException();
            }
        } else {
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        Collection<ChessMove> possibleOpponentMoves = new ArrayList<>();
        Collection<ChessPosition> opponentEndPositions = new ArrayList<>();
        if (teamColor == TeamColor.WHITE) {
            possibleOpponentMoves.addAll(allPossibleMoves(TeamColor.BLACK));
        } else {
            possibleOpponentMoves.addAll(allPossibleMoves(TeamColor.WHITE));
        }
        for (ChessMove chessMove : possibleOpponentMoves) {
            opponentEndPositions.add(chessMove.getEndPosition());
        }
        return opponentEndPositions.contains(kingPosition);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return allValidMoves(teamColor).isEmpty() && isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return allValidMoves(teamColor).isEmpty() && !isInCheck(teamColor);
    }

    // Helper function
    public Collection<ChessMove> allPossibleMoves(TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPosition = new ChessPosition(row, col);
                if (gameBoard.getPiece(currPosition) != null) {
                    if (gameBoard.getPiece(currPosition).getTeamColor() == teamColor) {
                        possibleMoves.addAll(gameBoard.getPiece(currPosition).pieceMoves(gameBoard, currPosition));
                    }
                }
            }
        }
        return possibleMoves;
    }

    // Helper function
    public Collection<ChessMove> allValidMoves(TeamColor teamColor) {
        Collection<ChessMove> allValidMoves = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPosition = new ChessPosition(row, col);
                if (gameBoard.getPiece(currPosition) != null) {
                    if (gameBoard.getPiece(currPosition).getTeamColor() == teamColor) {
                        allValidMoves.addAll(validMoves(currPosition));
                    }
                }
            }
        }
        return allValidMoves;
    }

    // Helper function
    private ChessPosition findKingPosition(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currPosition = new ChessPosition(row, col);
                if (gameBoard.getPiece(currPosition) != null) {
                    if (gameBoard.getPiece(currPosition).getPieceType() == ChessPiece.PieceType.KING
                            && gameBoard.getPiece(currPosition).getTeamColor() == teamColor) {
                        kingPosition = currPosition;
                    }
                }
            }
        }
        return kingPosition;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}
