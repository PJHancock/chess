package service;

import service.requests.ClearRequest;
import service.results.ClearResult;
import dataaccess.*;

public class ClearService {
    public ClearResult clear(ClearRequest request) {
        AuthDAO auth = new AuthDAO();
        auth.clear();
        GameDAO game = new GameDAO();
        game.clear();
        UserDAO user = new UserDAO();
        user.clear();
        return null;
    }
}
