import chess.*;
import server.Server;
import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        Server testing_page = new Server();
        testing_page.run(8080);
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}