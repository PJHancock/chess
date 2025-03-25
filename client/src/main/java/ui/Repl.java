package ui;

import ui.Clients.PostloginClient;
import ui.Clients.PreloginClient;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;
    // private final GameplayClient gameplayClient;
    private static final Scanner scanner = new Scanner(System.in);
    private static String result = "";


    public Repl(String serverUrl) {
        preloginClient = new PreloginClient(serverUrl);
        postloginClient = new PostloginClient(serverUrl);
        // gameplayClient = new GameplayClient(serverUrl);
    }

    public void runPrelogin() {
        System.out.println(SET_TEXT_COLOR_WHITE + WHITE_PAWN + RESET_TEXT_COLOR +
                " Welcome to 240 chess. Type Help to get started" +
                SET_TEXT_COLOR_WHITE + WHITE_PAWN + RESET_TEXT_COLOR);
        while (!result.equals("quit")) {
            printPreloginPrompt();
            String line = scanner.nextLine();
            try {
                result = preloginClient.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
                handlePostLogin(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
            System.out.println();
        }
    }

    private void handlePostLogin(String result) {
        if (result.split(" ")[0].equals("Logged")) {
            String authToken = preloginClient.getAuthToken();
            runPostlogin(authToken);
        }
    }

    public void runPostlogin(String authToken) {
        while (!(result.equals("Logged out") || result.equals("quit"))) {
            printPostloginPrompt();
            String line = scanner.nextLine();

            try {
                result = postloginClient.eval(line, authToken);
                System.out.print(result);
                if (result.split(" ")[0].equals( "Joined")) {
                    // Pass in if joining as white or black
                    printGameboard(line.split(" ")[2]);
                    // runGameplay();
                } else if (result.split(" ")[0].equals( "Watching")) {
                    printGameboard("white");
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    public void printGameboard(String playerSide) {
        String backgroundColor1;
        String backgroundColor2;
        String leftBlackPiece;
        String rightBlackPiece;
        String leftWhitePiece;
        String rightWhitePiece;
        if (playerSide.equals("white")) {
            backgroundColor1 = SET_BG_COLOR_LIGHT_GREY;
            backgroundColor2 = SET_BG_COLOR_BLACK;
            leftBlackPiece = BLACK_KING;
            rightBlackPiece = BLACK_QUEEN;
            leftWhitePiece = WHITE_KING;
            rightWhitePiece = WHITE_QUEEN;
        } else {
            backgroundColor1 = SET_BG_COLOR_BLACK;
            backgroundColor2 = SET_BG_COLOR_LIGHT_GREY;
            leftBlackPiece = BLACK_QUEEN;
            rightBlackPiece = BLACK_KING;
            leftWhitePiece = WHITE_QUEEN;
            rightWhitePiece = WHITE_KING;
        }
        String blackColumns =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "   " +
                        "  " + "h" + "  " +
                        "  " + "g" + "  " +
                        "  " + "f" + "  " +
                        "  " + "e" + "  " +
                        "  " + "d" + "  " +
                        "  " + "c" + "  " +
                        "  " + "b" + "  " +
                        "  " + "a" + "  " +
                        "   " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String whiteColumns =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "   " +
                "  " + "a" + "  " +
                "  " + "b" + "  " +
                "  " + "c" + "  " +
                "  " + "d" + "  " +
                "  " + "e" + "  " +
                "  " + "f" + "  " +
                "  " + "g" + "  " +
                "  " + "h" + "  " +
                "   " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line8 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 8 " +
                        SET_TEXT_COLOR_BLUE + backgroundColor1 + " " + BLACK_ROOK + " " +
                        backgroundColor2 + " " + BLACK_KNIGHT + " " +
                        backgroundColor1 + " " + BLACK_BISHOP + " " +
                        backgroundColor2 + " " + leftBlackPiece + " " +
                        backgroundColor1 + " " + rightBlackPiece + " " +
                        backgroundColor2 + " " + BLACK_BISHOP + " " +
                        backgroundColor1 + " " + BLACK_KNIGHT + " " +
                        backgroundColor2 + " " + BLACK_ROOK + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 8 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line7 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 7 " +
                        SET_TEXT_COLOR_BLUE + backgroundColor2 + " " + BLACK_PAWN + " " +
                        backgroundColor1 + " " + BLACK_PAWN + " " +
                        backgroundColor2 + " " + BLACK_PAWN + " " +
                        backgroundColor1 + " " + BLACK_PAWN + " " +
                        backgroundColor2 + " " + BLACK_PAWN + " " +
                        backgroundColor1 + " " + BLACK_PAWN + " " +
                        backgroundColor2 + " " + BLACK_PAWN + " " +
                        backgroundColor1 + " " + BLACK_PAWN + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 7 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line6 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 6 " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 6 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line5 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 5 " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 5 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line4 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 4 " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 4 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line3 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 3 " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        backgroundColor2 + " " + EMPTY + " " +
                        backgroundColor1 + " " + EMPTY + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 3 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line2 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 " +
                        SET_TEXT_COLOR_RED + backgroundColor1 + " " + WHITE_PAWN + " " +
                        backgroundColor2 + " " + WHITE_PAWN + " " +
                        backgroundColor1 + " " + WHITE_PAWN + " " +
                        backgroundColor2 + " " + WHITE_PAWN + " " +
                        backgroundColor1 + " " + WHITE_PAWN + " " +
                        backgroundColor2 + " " + WHITE_PAWN + " " +
                        backgroundColor1 + " " + WHITE_PAWN + " " +
                        backgroundColor2 + " " + WHITE_PAWN + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        String line1 =  SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 1 " +
                        SET_TEXT_COLOR_RED + backgroundColor2 + " " + WHITE_ROOK + " " +
                        backgroundColor1 + " " + WHITE_KNIGHT + " " +
                        backgroundColor2 + " " + WHITE_BISHOP + " " +
                        backgroundColor1 + " " + leftWhitePiece + " " +
                        backgroundColor2 + " " + rightWhitePiece + " " +
                        backgroundColor1 + " " + WHITE_BISHOP + " " +
                        backgroundColor2 + " " + WHITE_KNIGHT + " " +
                        backgroundColor1 + " " + WHITE_ROOK + " " +
                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 1 " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n";

        if (playerSide.equals("white")) {
            System.out.print("\n" + whiteColumns + line8 + line7 + line6 + line5 + line4 + line3 + line2 + line1 + whiteColumns + RESET_TEXT_COLOR + RESET_BG_COLOR);
        } else {
            System.out.print("\n" + blackColumns + line1 + line2 + line3 + line4 + line5 + line6 + line7 + line8 + blackColumns + RESET_TEXT_COLOR + RESET_BG_COLOR);
        }
    }

    private void printPreloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_OUT] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPostloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_IN] >>> " + SET_TEXT_COLOR_GREEN);
    }
}
