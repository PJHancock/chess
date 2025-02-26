package service;

import dataaccess.Memory.*;
import service.requests.ClearRequest;
import service.results.ClearResult;

public class ClearService {
    public ClearResult clear(ClearRequest request) {
        MemoryAuthDAO auth = new MemoryAuthDAO();
        auth.clear();
        MemoryGameDAO game = new MemoryGameDAO();
        game.clear();
        MemoryUserDAO user = new MemoryUserDAO();
        user.clear();
        return null;
    }
}
