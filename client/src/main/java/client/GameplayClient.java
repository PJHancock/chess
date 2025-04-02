package client;

import model.GameData;
import ui.DataAccessException;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class GameplayClient {
    private final ServerFacade server;
    // private final String serverUrl;

    public GameplayClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        // this.serverUrl = serverUrl;
    }

    public String eval(String input, GameData gameData, String teamColor) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw(gameData, teamColor);
                case "move" -> move(gameData, teamColor, params);
                case "highlight" -> highlight(gameData, params);
                case "leave" -> leave();
                case "resign" -> resign();
                default -> help();
            };
        } catch (DataAccessException ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
        }
    }

    public String help() {
        return SET_TEXT_COLOR_BLUE + "redraw " +
                RESET_TEXT_COLOR + "- chess board\n" +
                SET_TEXT_COLOR_BLUE + "move <x1,y1> <x2,y2> " +
                RESET_TEXT_COLOR + "- piece from <x1,y1> to <x2,y2>\n" +
                SET_TEXT_COLOR_BLUE + "highlight <x,y> " +
                RESET_TEXT_COLOR + "- possible moves from piece at <x1,y1> \n" +
                SET_TEXT_COLOR_BLUE + "leave " +
                RESET_TEXT_COLOR + "- the game \n" +
                SET_TEXT_COLOR_BLUE + "resign " +
                RESET_TEXT_COLOR + "- if you want to forfeit \n" +
                SET_TEXT_COLOR_BLUE + "help " +
                RESET_TEXT_COLOR + "- with possible commands";
    }

    private String resign() {
        return "Do you want to resign? (Y)es/(N)o ";
    }

    private String leave() {
        return "You left the game";
    }

    private String highlight(GameData gameData, String[] params) throws DataAccessException {
        return null;
    }

    private String redraw(GameData gameData, String teamColor) {
        return null;
    }

    private String move(GameData gameData, String teamColor, String... params) throws DataAccessException {
        return null;
    }
}
