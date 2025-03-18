package ui;

import ui.Clients.GameplayClient;
import ui.Clients.PostloginClient;
import ui.Clients.PreloginClient;

import java.util.Objects;
import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;
    private final GameplayClient gameplayClient;


    public Repl(String serverUrl) {
        preloginClient = new PreloginClient(serverUrl);
        postloginClient = new PostloginClient(serverUrl);
        gameplayClient = new GameplayClient(serverUrl);
    }

    public void runPrelogin() {
        System.out.println(WHITE_PAWN + " Welcome to 240 chess. Type help to get started" + WHITE_PAWN);
        System.out.print(preloginClient.help());
        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPreloginPrompt();
            String line = scanner.nextLine();

            try {
                result = preloginClient.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
                if (Objects.equals(line., "login") || Objects.equals(line, "register")) {
                    System.out.print("Logged in as ");
                    runPostlogin();
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
            System.out.println();
        }
    }

    public void runPostlogin() {
        System.out.print(postloginClient.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!(result.equals("logout") || result.equals("quit"))) {
            printPostloginPrompt();
            String line = scanner.nextLine();

            try {
                result = postloginClient.eval(line);
                System.out.print(result);
                if (Objects.equals(line., "join") || Objects.equals(line, "observe")) {
                    runGameplay();
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void runGameplay() {
        printGameboard();
    }

    public void printGameboard() {

    }

    private void printPreloginPrompt() {
        System.out.print("\n" + "[LOGGED_OUT] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPostloginPrompt() {
        System.out.print("\n" + "[LOGGED_OUT] >>> " + SET_TEXT_COLOR_BLUE);
    }

    private void printGameplayPrompt() {
        // To be implemented layer
        System.out.print("\n" + "[GamePlay Mode]" + ">>> ");
    }
}
