package ui.Clients;

import ui.ServerFacade;

public class GameplayClient {
    private final ServerFacade server;
    private final String serverUrl;

    public GameplayClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }
}