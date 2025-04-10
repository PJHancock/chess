package ui;

import client.GameplayClient;
import client.PostloginClient;
import client.PreloginClient;
import websocket.NotificationHandler;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;
    private final GameplayClient gameplayClient;
    public static final Scanner SCANNER = new Scanner(System.in);
    private static String result = "";
    public static String teamColor;


    public Repl(String serverUrl) {
        preloginClient = new PreloginClient(serverUrl);
        postloginClient = new PostloginClient(serverUrl);
        gameplayClient = new GameplayClient(serverUrl, this);
    }

    public void notify(ServerMessage serverMessage) throws ui.DataAccessException {
        if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            System.out.println("\n" + serverMessage.getMessage());
            printGameplayPrompt();
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            System.out.println("\n" + SET_TEXT_COLOR_RED + serverMessage.getErrorMessage() + RESET_TEXT_COLOR);
            printGameplayPrompt();
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            System.out.println("\n" + gameplayClient.redrawBoard(serverMessage.getGameData(), teamColor));
            printGameplayPrompt();
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.HIGHLIGHT_GAME) {
            System.out.println("\n" + gameplayClient.highlightedBoard(serverMessage.getGameData(), teamColor, serverMessage.getPiecePosition()));
            printGameplayPrompt();
        }
    }

    public void runPrelogin() {
        System.out.println(SET_TEXT_COLOR_WHITE + WHITE_PAWN + RESET_TEXT_COLOR +
                " Welcome to 240 chess. Type Help to get started" +
                SET_TEXT_COLOR_WHITE + WHITE_PAWN + RESET_TEXT_COLOR);
        printPreloginPrompt();
        while (!result.equals("quit")) {
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
            printPreloginPrompt();
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
                    int gameId = PostloginClient.gameIds.get(Integer.parseInt(listGamesGameId));
                    gameplayClient.connectWebsocket(authToken, gameId);
                    teamColor = line.split(" ")[2];
                    runGameplay(authToken, gameId, teamColor);
                } else if (result.split(" ")[0].equals( "Watching")) {
                    String listGamesGameId = result.split(" ")[2];
                    int gameId = PostloginClient.gameIds.get(Integer.parseInt(listGamesGameId));
                    gameplayClient.connectWebsocket(authToken, gameId);
                    runGameplay(authToken, gameId, null);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    public void runGameplay(String authToken, int gameId, String teamColor) throws IOException {
        while (!(result.equals("You left the game") || result.equals("You stopped watching the game"))) {
            printGameplayPrompt();
            String line = SCANNER.nextLine();
            System.out.print(RESET_TEXT_COLOR);
            try {
                result = gameplayClient.eval(line, authToken, gameId, teamColor);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        gameplayClient.disconnectWebsocket();
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
