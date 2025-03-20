package ui.Clients;

import ui.DataAccessException;
import ui.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class PostloginClient {
    private final ServerFacade server;
    private final String serverUrl;

    public PostloginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create" -> create(params);
                case "list" -> list(params);
                case "join" -> join();
                case "observe" -> observe();
                case "logout" -> logout(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + " register <USERNAME> <PASSWORD> <EMAIL>" +
                RESET_TEXT_COLOR + "- to create an account\n" +
                SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" +
                RESET_TEXT_COLOR + "- to play chess\n" +
                SET_TEXT_COLOR_BLUE + "quit " +
                RESET_TEXT_COLOR + "- playing chess \n" +
                SET_TEXT_COLOR_BLUE + "help " +
                RESET_TEXT_COLOR + "- with possible commands";
    }
}