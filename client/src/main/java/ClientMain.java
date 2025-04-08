import dataaccess.DataAccessException;
import ui.Repl;

public class ClientMain {
    public static void main(String[] args) throws DataAccessException, ui.DataAccessException {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        new Repl(serverUrl).runPrelogin();
    }

}