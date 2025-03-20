package ui;

import com.google.gson.Gson;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.ListGamesData;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.io.*;
import java.net.*;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


    public String register(String username, String password, String email) throws DataAccessException {
        var path = "/user";
        var requestBody = new RegisterRequest(username, password, email);
        var response = this.makeRequest("Post", path, requestBody, RegisterResult.class);
        return response.username();
    }

    public String login(String username, String password) throws DataAccessException {
        var path = "/session";
        var requestBody = new LoginRequest(username, password);
        var response = this.makeRequest("Post", path, requestBody, LoginResult.class);
        return response.username();
    }

    public int create(String param) {
    }

    public List<ListGamesData> list() {
        return null;
    }

    public String join(String param, String param1) {
    }

    public String observe(String param) {
    }

    public String logout() {
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws DataAccessException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
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
