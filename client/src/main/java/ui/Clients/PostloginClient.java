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
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + " create <NAME> " +
                RESET_TEXT_COLOR + "- a game\n" +
                SET_TEXT_COLOR_BLUE + "list " +
                RESET_TEXT_COLOR + "- games\n" +
                SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK] " +
                RESET_TEXT_COLOR + "- a game \n" +
                SET_TEXT_COLOR_BLUE + "observe <ID> " +
                RESET_TEXT_COLOR + "- a game \n" +
                SET_TEXT_COLOR_BLUE + "logout " +
                RESET_TEXT_COLOR + "- when you are done \n" +
                SET_TEXT_COLOR_BLUE + "quit " +
                RESET_TEXT_COLOR + "- playing chess \n" +
                SET_TEXT_COLOR_BLUE + "help " +
                RESET_TEXT_COLOR + "- with possible commands";
    }

    public String create(String... params) throws DataAccessException {
        if (params.length == 1) {
            String username = server.create(params[0]);
            return String.format("Logged in as %s.", username);
        }
        throw new DataAccessException("Expected: <NAME>");
    }

    public String list(String... params) throws DataAccessException {
        String username = server.list();
        return String.format("Logged in as %s.", username);
    }

    public String join(String... params) throws DataAccessException {
        if (params.length == 2) {
            String username = server.join(params[0], params[1]);
            return String.format("Logged in as %s.", username);
        }
        throw new DataAccessException("Expected: <ID> [WHITE|BLACK]");
    }

    public String observe(String... params) throws DataAccessException {
        if (params.length == 1) {
            String username = server.observe(params[0]);
            return String.format("Logged in as %s.", username);
        }
        throw new DataAccessException("Expected: <ID>");
    }

    public String logout() throws DataAccessException {
        String username = server.logout();
        return "Logged out";
    }
}