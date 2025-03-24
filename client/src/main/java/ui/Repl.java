package ui;

import ui.Clients.PostloginClient;
import ui.Clients.PreloginClient;

import java.util.Objects;
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
        System.out.println(WHITE_PAWN + " Welcome to 240 chess. Type Help to get started" + WHITE_PAWN);
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
                if (Objects.equals(line.split(" ")[0], "join")) {
                    // Pass in if joining as white or black
                    printGameboard(line.split(" ")[2]);
                    // runGameplay();
                } else if (Objects.equals(line.split(" ")[0], "observe")) {
                    printGameboard("white");
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    public void printGameboard(String playerSide) {
        String line0 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + "    h  g  f  e  d  c  b  a    \n";
        String line1 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 1 " +
                        SET_TEXT_COLOR_BLUE + SET_BG_COLOR_LIGHT_GREY + " " + BLACK_ROOK + " " +
                        SET_BG_COLOR_LIGHT_GREY + " " + BLACK_KNIGHT + " " +
                        SET_BG_COLOR_BLACK + " " + BLACK_BISHOP + " " +
                        SET_BG_COLOR_LIGHT_GREY + " " + BLACK_QUEEN + " " +
                        SET_BG_COLOR_BLACK + " " + BLACK_KING + " " +
                        SET_BG_COLOR_LIGHT_GREY + " " + BLACK_BISHOP + " " +
                        SET_BG_COLOR_BLACK + " " + BLACK_KNIGHT + " " +
                        SET_BG_COLOR_LIGHT_GREY + " " + BLACK_ROOK + " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 1 \n";
        String line2 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 2 " +
                        " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 2 \n";
        String line3 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 3 " +
                        " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 3 \n";
        String line4 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 4 " +
                        " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 4 \n";
        String line5 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 5 " +
                        " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 5 \n";
        String line6 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 6 " +
                        " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 6 \n";
        String line7 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 7 " +
                        " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 7 \n";
        String line8 = SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 8 " +
                        " " +
                        SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + " 8 \n";
        if (playerSide.equals("white")) {
            System.out.print("\n" + line0 + line8 + line7 + line6 + line5 + line4 + line3 + line2 + line1 + line0);
        } else {
            System.out.print("\n" + line0 + line1 + line2 + line3 + line4 + line5 + line6 + line7 + line8 + line0);
        }
    }

    private void printPreloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_OUT] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPostloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_IN] >>> " + SET_TEXT_COLOR_GREEN);
    }
}
