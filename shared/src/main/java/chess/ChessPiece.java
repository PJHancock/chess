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
        if (board.getPiece(myPosition).getPieceType() == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        } else if (board.getPiece(myPosition).getPieceType() == PieceType.ROOK) {
            return rookMoves(board, myPosition);
        } else if (board.getPiece(myPosition).getPieceType() == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        } else if (board.getPiece(myPosition).getPieceType() == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        } else if (board.getPiece(myPosition).getPieceType() == PieceType.QUEEN) {
            return queenMoves(board, myPosition);
        } else {
            return kingMoves(board, myPosition);
        }
    }

    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, int[][] directions, boolean continuous){
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (int[] direction : directions) {

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                // Check for out of bounds
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }

                // Create new position
                ChessPosition newPosition = new ChessPosition(row, col);

                // Check for other pieces
                if (board.getPiece(newPosition) != null) {
                    if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                } else {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }

                if (!continuous) {
                    break;
                }
            }
        }
        return validMoves;
    }

    Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        int[][] directions = {{-1,-1},{-1,1},{1,1},{1,-1}};
        return calculateMoves(board, myPosition, directions, true);
    }

    Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
        return calculateMoves(board, myPosition, directions, true);
    }

    Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> validMoves = new ArrayList<>();
        validMoves.addAll(rookMoves(board, myPosition));
        validMoves.addAll(bishopMoves(board, myPosition));
        return validMoves;
    }

    Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        int[][] directions = {{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0}};
        return calculateMoves(board, myPosition, directions, false);
    }

    Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        int[][] directions = {{-2,-1},{-2,1},{-1,2},{1,2},{2,1},{2,-1},{1,-2},{-1,-2}};
        return calculateMoves(board, myPosition, directions, false);
    }

    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        // Case 1: White pawn
        if (board.getPiece(myPosition).pieceColor == ChessGame.TeamColor.WHITE) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            // take diagonal piece
            if (col+1 < 8) {
                if ((board.getPiece(new ChessPosition(row + 1, col + 1))) != null) {
                    ChessPosition newPosition = new ChessPosition(row + 1, col + 1);
                    // if promotion
                    if ((myPosition.getRow() == 7) && (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor()))) {
                        validMoves.addAll(promotionHelper(myPosition, newPosition));
                    } else if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            if (col-1 > 0) {
                if ((board.getPiece(new ChessPosition(row+1,col-1))) != null) {
                    ChessPosition newPosition = new ChessPosition(row+1, col-1);
                    // if promotion
                    if ((myPosition.getRow() == 7) && (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor()))) {
                        validMoves.addAll(promotionHelper(myPosition, newPosition));
                    } else if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            // first move extra option
            if (myPosition.getRow() == 2) {
                ChessPosition newPosition = new ChessPosition(row+2, col);
                if ((board.getPiece(new ChessPosition(row+1, col)) == null) && (board.getPiece(newPosition) == null)) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
            // promotion move
            ChessPosition newPosition = new ChessPosition(row+1, col);
            if (myPosition.getRow() == 7) {
                if (board.getPiece(newPosition) == null) {
                    validMoves.addAll(promotionHelper(myPosition, newPosition));
                }
            } else {
                if (board.getPiece(newPosition) == null) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        } else {
            // Case 2: Black pawn
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            // take diagonal piece
            if (col+1 < 8) {
                if ((board.getPiece(new ChessPosition(row - 1, col + 1))) != null) {
                    ChessPosition newPosition = new ChessPosition(row - 1, col + 1);
                    // if promotion
                    if ((myPosition.getRow() == 2) && (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor()))) {
                        validMoves.addAll(promotionHelper(myPosition, newPosition));
                    } else if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            if (col-1 > 0) {
                if ((board.getPiece(new ChessPosition(row-1,col-1))) != null) {
                    ChessPosition newPosition = new ChessPosition(row-1, col-1);
                    // if promotion
                    if ((myPosition.getRow() == 2) && (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor()))) {
                        validMoves.addAll(promotionHelper(myPosition, newPosition));
                    } else if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            // first move extra option
            if (myPosition.getRow() == 7) {
                ChessPosition newPosition = new ChessPosition(row-2, col);
                if ((board.getPiece(new ChessPosition(row-1, col)) == null) && (board.getPiece(newPosition) == null)) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
            // promotion move
            ChessPosition newPosition = new ChessPosition(row-1, col);
            if (myPosition.getRow() == 2) {
                if (board.getPiece(newPosition) == null) {
                    validMoves.addAll(promotionHelper(myPosition, newPosition));
                }
            } else {
                if (board.getPiece(newPosition) == null) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        // Check to see if out of range
        return validMoves;
    }

    public Collection<ChessMove> promotionHelper(ChessPosition myPosition, ChessPosition newPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
        return validMoves;
    }

    @Override
    public String toString() {
        return "[" + pieceColor + ' ' + type.name() + ']';
    }
}
