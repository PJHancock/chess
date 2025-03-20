package ui.Clients;

import ui.DataAccessException;
import ui.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class PreloginClient {
    private final ServerFacade server;
    // private final String serverUrl;

    public PreloginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        // this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
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

    public String register(String... params) throws DataAccessException {
        if (params.length == 3) {
            String username = server.register(params[0], params[1], params[2]);
            return String.format("Logged in as %s.", username);
        }
        throw new DataAccessException("Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String login(String... params) throws DataAccessException {
        if (params.length == 2) {
            String username = server.login(params[0], params[1]);
            return String.format("Logged in as %s.", username);
        }
        throw new DataAccessException("Expected: <USERNAME> <PASSWORD>");
    }
}
