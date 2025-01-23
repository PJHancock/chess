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
        return validMoves;
    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int [][] directions = {{-1,-1}, {-1,1},{1,1},{1,-1}};
        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                // Check to see if out of range
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, col);

                // Check for pieces in the way
                if (board.getPiece(newPosition) != null) {
                    if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
                validMoves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return validMoves;
    }

    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int [][] directions = {{-2,1}, {-2,-1},{2,1},{2,-1},{1,-2},{1,2},{-1,-2},{-1,2}};
        for (int[] direction : directions) {

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            row += direction[0];
            col += direction[1];

            // Check to see if out of range
            if (row < 1 || row > 8 || col < 1 || col > 8) {
                continue;
            }

            ChessPosition newPosition = new ChessPosition(row, col);

            // Check for pieces in the way
            if (board.getPiece(newPosition) != null) {
                if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
                continue;
            }
            validMoves.add(new ChessMove(myPosition, newPosition, null));
            }
        return validMoves;
    }

    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int [][] directions = {{-1,0}, {1,0},{0,1},{0,-1}};
        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                // Check to see if out of range
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, col);

                // Check for pieces in the way
                if (board.getPiece(newPosition) != null) {
                    if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
                validMoves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return validMoves;
    }

    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int [][] directions = {{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1}, {-1,0}};
        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            row += direction[0];
            col += direction[1];

            // Check to see if out of range
            if (row < 1 || row > 8 || col < 1 || col > 8) {
                continue;
            }

            ChessPosition newPosition = new ChessPosition(row, col);

            // Check for pieces in the way
            if (board.getPiece(newPosition) != null) {
                if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
                continue;
            }
            validMoves.add(new ChessMove(myPosition, newPosition, null));
        }
        return validMoves;
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
                    if (myPosition.getRow() == 7) {
                        if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                        }
                    } else if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            if (col-1 > 0) {
                if ((board.getPiece(new ChessPosition(row+1,col-1))) != null) {
                    ChessPosition newPosition = new ChessPosition(row+1, col-1);
                    // if promotion
                    if (myPosition.getRow() == 7) {
                        if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                        }
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
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
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
                    if (myPosition.getRow() == 2) {
                        if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                        }
                    } else if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            if (col-1 > 0) {
                if ((board.getPiece(new ChessPosition(row-1,col-1))) != null) {
                    ChessPosition newPosition = new ChessPosition(row-1, col-1);
                    // if promotion
                    if (myPosition.getRow() == 2) {
                        if (board.getPiece(newPosition).pieceColor != (board.getPiece(myPosition).getTeamColor())) {
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                            validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                        }
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
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
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
}