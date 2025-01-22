package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */


    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
         if (getPieceType() == PieceType.QUEEN) {
             return queenMoves(board, myPosition);
         } else if (getPieceType() == PieceType.BISHOP) {
             return bishopMoves(board, myPosition);
         } else if (getPieceType() == PieceType.KNIGHT) {
             return knightMoves(board, myPosition);
         } else if (getPieceType() == PieceType.ROOK) {
             return rookMoves(board, myPosition);
         } else if (getPieceType() == PieceType.PAWN) {
             return pawnMoves(board, myPosition);
         } else {
             return kingMoves(board, myPosition);
         }
    }

    public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        validMoves.addAll(bishopMoves(board, myPosition));
        validMoves.addAll(rookMoves(board, myPosition));
        validMoves.addAll(kingMoves(board, myPosition));
        return validMoves;
    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        // add moves diagonally up to the right
        for (int i = myPosition.getRow(); i <= 8; i++) {
            for (int j = myPosition.getColumn(); j <= 8; j++) {
                if (board.getPiece(new ChessPosition(i,j)) != null) {
                    break;
                } else {
                    validMoves.add(new ChessMove(myPosition, new ChessPosition(i,j), null));
                }
            }
        }
        // add moves diagonally up to the left
        for (int i = myPosition.getRow(); i <= 8; i++) {
            for (int j = myPosition.getColumn(); j >= 1; j--) {
                if (board.getPiece(new ChessPosition(i,j)) != null) {
                    break;
                } else {
                    validMoves.add(new ChessMove(myPosition, new ChessPosition(i,j), null));
                }
            }
        }
        // add moves diagonally down to the right
        for (int i = myPosition.getRow(); i >= 1; i--) {
            for (int j = myPosition.getColumn(); j <= 8; j++) {
                if (board.getPiece(new ChessPosition(i,j)) != null) {
                    break;
                } else {
                    validMoves.add(new ChessMove(myPosition, new ChessPosition(i,j), null));
                }
            }
        }
        // add moves diagonally down to the left
        for (int i = myPosition.getRow(); i >= 1; i--) {
            for (int j = myPosition.getColumn(); j >= 1; j--) {
                if (board.getPiece(new ChessPosition(i,j)) != null) {
                    break;
                } else {
                    validMoves.add(new ChessMove(myPosition, new ChessPosition(i,j), null));
                }
            }
        }
        return validMoves;
    }

    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        return validMoves;
    }

    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        return validMoves;
    }

    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        return validMoves;
    }

    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        return validMoves;
    }
}