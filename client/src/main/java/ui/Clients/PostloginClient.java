package ui.Clients;

import ui.ServerFacade;

public class PostloginClient {
    private final ServerFacade server;
    private final String serverUrl;

    public PostloginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }
}