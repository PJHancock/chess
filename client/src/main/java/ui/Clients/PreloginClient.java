package ui.Clients;

import ui.ServerFacade;

public class PreloginClient {
    private final ServerFacade server;
    private final String serverUrl;

    public PreloginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }
}
