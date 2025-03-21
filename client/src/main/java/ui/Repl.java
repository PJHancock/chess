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
                if (Objects.equals(line.split(" ")[0], "join") || Objects.equals(line.split(" ")[0], "observe")) {
                    // Pass in if joining as white or black
                    printGameboard(line.split(" ")[2]);
                    // runGameplay();
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void printGameboard(String playerSide) {

    }

    private void printPreloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_OUT] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPostloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_IN] >>> " + SET_TEXT_COLOR_GREEN);
    }
}
