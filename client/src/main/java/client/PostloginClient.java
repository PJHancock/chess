package client;

import model.results.ListGamesData;
import ui.DataAccessException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static ui.EscapeSequences.*;

public class PostloginClient {
    private final ServerFacade server;
    // private final String serverUrl;
    private HashMap<Integer, Integer> gameIds = new HashMap<>();

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
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
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
            int gameID = server.create(authToken, params[0]);
            gameIds.put(gameIds.size() + 1, gameID);
            return "Created game with name " + SET_TEXT_COLOR_RED + params[0] + RESET_TEXT_COLOR;
        }
        throw new DataAccessException(SET_TEXT_COLOR_RED + "Expected: <NAME>" + RESET_TEXT_COLOR);
    }

    public String list(String authToken) throws DataAccessException {
        List<ListGamesData> games = server.list(authToken);
        gameIds = new HashMap<>();
        var result = new StringBuilder();
        int index = 1;
        if (games.isEmpty()) {
            return "No active games";
        }
        for (var game : games) {
            String gameInfo = getGameInfo(game, index);
            result.append(gameInfo).append('\n');
            gameIds.put(index, game.gameID());
            index += 1;
        }
        return result.toString();
    }

    private static String getGameInfo(ListGamesData game, int index) {
        String gameName = game.gameName();
        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();
        return RESET_TEXT_COLOR + "ID: " + SET_TEXT_COLOR_RED + index + RESET_TEXT_COLOR +
                " Game name: " + SET_TEXT_COLOR_RED + gameName + RESET_TEXT_COLOR +
                " White player: " + SET_TEXT_COLOR_RED + whiteUsername + RESET_TEXT_COLOR +
                " Black player: " + SET_TEXT_COLOR_RED + blackUsername + RESET_TEXT_COLOR;
    }

    public String join(String authToken, String... params) throws DataAccessException {
        if (params.length == 2) {
            int gameId = gameIds.get(Integer.parseInt(params[0]));
            server.join(authToken, gameId, params[1]);
            return "Joined game";
        }
        throw new DataAccessException(SET_TEXT_COLOR_RED + "Expected: <ID> [WHITE|BLACK]" + RESET_TEXT_COLOR);
    }

    public String observe(String... params) throws DataAccessException {
        if (params.length == 1) {
            if (Integer.parseInt(params[0]) > gameIds.size()) {
                throw new DataAccessException(SET_TEXT_COLOR_RED + "Invalid game ID" + RESET_TEXT_COLOR);
            }
            int gameId = gameIds.get(Integer.parseInt(params[0]));
            return "Watching game " + gameId;
        }
        throw new DataAccessException(SET_TEXT_COLOR_RED + "Expected: <ID>" + RESET_TEXT_COLOR);
    }

    public String logout(String authToken) throws DataAccessException {
        server.logout(authToken);
        return "Logged out";
    }
}