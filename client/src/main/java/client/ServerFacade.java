package client;

import chess.ChessGame;
import com.google.gson.Gson;
import model.requests.CreateGameRequest;
import model.requests.JoinGameRequest;
import model.requests.LoginRequest;
import model.requests.RegisterRequest;
import model.results.*;
import ui.DataAccessException;
import java.io.*;
import java.net.*;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


    public RegisterResult register(String username, String password, String email) throws DataAccessException {
        try {
            var path = "/user";
            var requestBody = new RegisterRequest(username, password, email);
            return this.makeRequest("POST", path, requestBody, RegisterResult.class, null);
        } catch (DataAccessException e){
            throw new DataAccessException("Username taken");
        }
    }

    public LoginResult login(String username, String password) throws DataAccessException {
        var path = "/session";
        var requestBody = new LoginRequest(username, password);
        return this.makeRequest("POST", path, requestBody, LoginResult.class, null);
    }

    public int create(String authToken, String gameName) throws DataAccessException {
        var path = "/game";
        var requestBody = new CreateGameRequest(gameName, authToken);
        var response = this.makeRequest("POST", path, requestBody, CreateGameResult.class, authToken);
        return response.gameID();
    }

    public List<ListGamesData> list(String authToken) throws DataAccessException {
        var path = "/game";
        var response = this.makeRequest("GET", path, null, ListGamesResult.class, authToken);
        return response.games();
    }

    public void join(String authToken, int gameID, String playerColor) throws DataAccessException {
        try {
            if (playerColor == null || !(playerColor.equals("white") || (playerColor.equals("black")))) {
                throw new DataAccessException("Invalid playerColor");
            }
            var path = "/game";
            JoinGameRequest requestBody;
            if ("white".equalsIgnoreCase(playerColor.trim())) {
                requestBody = new JoinGameRequest(ChessGame.TeamColor.WHITE, gameID, authToken);
            } else {
                requestBody = new JoinGameRequest(ChessGame.TeamColor.BLACK, gameID, authToken);
            }
            this.makeRequest("PUT", path, requestBody, JoinGameResult.class, authToken);
        } catch (DataAccessException e){
            if (e.getMessage().equals("Request failed: Forbidden")) {
                throw new DataAccessException("Team color already taken");
            }
            throw new DataAccessException(e.getMessage());
        }
    }

    public void logout(String authToken) throws DataAccessException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String userAuthToken) throws DataAccessException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            http.addRequestProperty("Content-Type", "application/json");

            if (userAuthToken != null && !userAuthToken.isEmpty()) {
                http.setRequestProperty("Authorization", userAuthToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new DataAccessException("Request failed: " + ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, DataAccessException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw new DataAccessException(http.getResponseMessage());
                }
            }

            throw new DataAccessException("other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
