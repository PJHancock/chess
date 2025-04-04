package client;

import dataaccess.sql.MySqlGameDao;
import model.GameData;
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
    private MySqlGameDao gameDao = new MySqlGameDao();

    public PostloginClient(String serverUrl) throws dataaccess.DataAccessException {
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
        StringBuilder gameName = new StringBuilder();
        for (String param : params) {
            gameName.append(param).append(" ");
        }
        gameName.deleteCharAt(gameName.length() - 1);
        int gameID = server.create(authToken, gameName.toString());
        gameIds.put(gameIds.size() + 1, gameID);
        return "Created game with name " + SET_TEXT_COLOR_GREEN + gameName + RESET_TEXT_COLOR;
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
        if (whiteUsername == null) {
            whiteUsername = "Empty";
        }
        String blackUsername = game.blackUsername();
        if (blackUsername == null) {
            blackUsername = "Empty";
        }
        return RESET_TEXT_COLOR + "ID: " + SET_TEXT_COLOR_GREEN + index + RESET_TEXT_COLOR +
                " Game name: " + SET_TEXT_COLOR_GREEN + gameName + RESET_TEXT_COLOR +
                " White player: " + SET_TEXT_COLOR_GREEN + whiteUsername + RESET_TEXT_COLOR +
                " Black player: " + SET_TEXT_COLOR_GREEN + blackUsername + RESET_TEXT_COLOR;
    }

    public String join(String authToken, String... params) throws DataAccessException {
        try {
            if (params.length == 2) {
                String playerColor = params[1];
                if (Integer.parseInt(params[0]) > gameIds.size() || Integer.parseInt(params[0]) <= 0) {
                    throw new DataAccessException("Invalid game ID");
                } else if (playerColor == null || !(playerColor.equals("white") || (playerColor.equals("black")))) {
                    throw new DataAccessException("Invalid playerColor");
                }
                int gameId = gameIds.get(Integer.parseInt(params[0]));
                server.join(authToken, gameId, params[1]);
                return "Joined game " + params[0];
            }
            throw new DataAccessException(SET_TEXT_COLOR_RED + "Expected: <ID> [WHITE|BLACK]" + RESET_TEXT_COLOR);
        } catch (DataAccessException e) {
            throw new DataAccessException(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
        } catch (NumberFormatException e) {
            throw new DataAccessException(SET_TEXT_COLOR_RED + "Invalid game ID" + RESET_TEXT_COLOR);
        }
    }

    public String observe(String... params) throws DataAccessException {
        if (params.length == 1) {
            if (Integer.parseInt(params[0]) > gameIds.size()) {
                throw new DataAccessException(SET_TEXT_COLOR_RED + "Invalid game ID" + RESET_TEXT_COLOR);
            }
            // int gameId = gameIds.get(Integer.parseInt(params[0]));
            return "Watching game " + params[0];
        }
        throw new DataAccessException(SET_TEXT_COLOR_RED + "Expected: <ID>" + RESET_TEXT_COLOR);
    }

    public GameData getGameData(String listGamesGameId) throws dataaccess.DataAccessException {
        int gameId = gameIds.get(Integer.parseInt(listGamesGameId));
        return gameDao.getGameUsingId(String.valueOf(gameId));
    }

    public String logout(String authToken) throws DataAccessException {
        server.logout(authToken);
        return "Logged out";
    }
}