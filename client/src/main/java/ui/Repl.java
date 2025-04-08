package ui;

import client.GameplayClient;
import client.PostloginClient;
import client.PreloginClient;
import dataaccess.DataAccessException;
import model.GameData;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;
    private final GameplayClient gameplayClient;
    public static final Scanner SCANNER = new Scanner(System.in);
    private static String result = "";


    public Repl(String serverUrl) throws DataAccessException, ui.DataAccessException {
        preloginClient = new PreloginClient(serverUrl);
        postloginClient = new PostloginClient(serverUrl);
        gameplayClient = new GameplayClient(serverUrl, this);
    }

    public void notify(NotificationHandler notification) {
        System.out.println(SET_TEXT_COLOR_RED + notification.toString() + RESET_TEXT_COLOR);
        printGameplayPrompt();
    }

    public void runPrelogin() {
        System.out.println(SET_TEXT_COLOR_WHITE + WHITE_PAWN + RESET_TEXT_COLOR +
                " Welcome to 240 chess. Type Help to get started" +
                SET_TEXT_COLOR_WHITE + WHITE_PAWN + RESET_TEXT_COLOR);
        while (!result.equals("quit")) {
            printPreloginPrompt();
            String line = SCANNER.nextLine();
            System.out.print(RESET_TEXT_COLOR);
            try {
                result = preloginClient.eval(line);
                System.out.print(result);
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
            String line = SCANNER.nextLine();
            System.out.print(RESET_TEXT_COLOR);
            try {
                result = postloginClient.eval(line, authToken);
                System.out.print(result);
                if (result.split(" ")[0].equals("Joined")) {
                    // Pass in if joining as white or black
                    String listGamesGameId = result.split(" ")[2];
                    GameData gameData = postloginClient.getGameData(listGamesGameId);
                    gameplayClient.connectWebsocket(authToken, gameData.gameID());
                    runGameplay(authToken, gameData, line.split(" ")[2]);
                } else if (result.split(" ")[0].equals( "Watching")) {
                    String listGamesGameId = result.split(" ")[2];
                    GameData gameData = postloginClient.getGameData(listGamesGameId);
                    gameplayClient.connectWebsocket(authToken, gameData.gameID());
                    runGameplay(authToken, gameData, null);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    public void runGameplay(String authToken, GameData gameData, String teamColor) {
        System.out.print("\n" + gameplayClient.redraw(gameData, teamColor));

        while (!(result.equals("You left the game") || result.equals("You stopped watching the game"))) {
            printGameplayPrompt();
            String line = SCANNER.nextLine();
            System.out.print(RESET_TEXT_COLOR);
            try {
                result = gameplayClient.eval(line, authToken, gameData, teamColor);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    private void printPreloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_OUT] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPostloginPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_IN] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printGameplayPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[Gameplay] >>> " + SET_TEXT_COLOR_GREEN);
    }
}
