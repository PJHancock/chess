package ui.Clients;

import com.google.gson.Gson;
import service.results.ListGamesData;
import ui.DataAccessException;
import ui.ServerFacade;

import java.util.Arrays;
import java.util.List;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class PostloginClient {
    private final ServerFacade server;
    // private final String serverUrl;

    public PostloginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        // this.serverUrl = serverUrl;
    }

    public String eval(String input, String authToken) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create" -> create(authToken, params);
                case "list" -> list(authToken);
                case "join" -> join(authToken, params);
                case "observe" -> observe(params);
                case "logout" -> logout(authToken);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + "create <NAME> " +
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

    public String create(String authToken, String... params) throws DataAccessException {
        if (params.length == 1) {
            int gameID = server.create(params[0], authToken);
            return String.format("Created game %s with game ID %d.", params[0], gameID);
        }
        throw new DataAccessException("Expected: <NAME>");
    }

    public String list(String authToken) throws DataAccessException {
        List<ListGamesData> games = server.list(authToken);
        var result = new StringBuilder();
        var gson = new Gson();
        for (var game : games) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String join(String authToken, String... params) throws DataAccessException {
        if (params.length == 2) {
            server.join(authToken, params[0], params[1]);
            return "Joined game";
        }
        throw new DataAccessException("Expected: <ID> [WHITE|BLACK]");
    }

    public String observe(String... params) throws DataAccessException {
        if (params.length == 1) {
            return String.format("Watching game %s", params[0]);
        }
        throw new DataAccessException("Expected: <ID>");
    }

    public String logout(String authToken) throws DataAccessException {
        server.logout(authToken);
        return "Logged out";
    }
}