package ui.Clients;

import service.results.LoginResult;
import service.results.RegisterResult;
import ui.DataAccessException;
import ui.ServerFacade;

import java.util.Arrays;
import static ui.EscapeSequences.*;

public class PreloginClient {
    private final ServerFacade server;
    private String authToken;
    // private final String serverUrl;

    public PreloginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        // this.serverUrl = serverUrl;
    }

    public String getAuthToken() {
        return authToken;
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
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
        }
    }

    public String help() {
        return  SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL> " +
                RESET_TEXT_COLOR + "- to create an account\n" +
                SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD> " +
                RESET_TEXT_COLOR + "- to play chess\n" +
                SET_TEXT_COLOR_BLUE + "quit " +
                RESET_TEXT_COLOR + "- playing chess \n" +
                SET_TEXT_COLOR_BLUE + "help " +
                RESET_TEXT_COLOR + "- with possible commands";
    }

    public String register(String... params) throws DataAccessException {
        if (params.length == 3) {
            RegisterResult result = server.register(params[0], params[1], params[2]);
            authToken = result.authToken();
            return "Logged in as " + result.username();
        }
        throw new DataAccessException("Invalid registration. Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String login(String... params) throws DataAccessException {
        if (params.length == 2) {
            LoginResult result = server.login(params[0], params[1]);
            authToken = result.authToken();
            return "Logged in as " + result.username();
        }
        throw new DataAccessException("Invalid login. Expected: <USERNAME> <PASSWORD>");
    }
}
