package nl.saxion.ptbc.frog_pilot.TestConnection;

import nl.saxion.ptbc.frog_pilot.UserInterfaceTP;
import nl.saxion.ptbc.sockets.SasaServer;
import nl.saxion.ptbc.sockets.SasaSocketInformation;

/**
 * !! ALWAYS RUN THIS FIRST, THEN GROUND CONTROL */
public class ConnectionTestTP implements SasaSocketInformation {
    private final SasaServer testServer;
    private static ConnectionTestTP tpConnectionTest;
    private static UserInterfaceTP UITP;
    private static final int PORT = 50000;

    public ConnectionTestTP(int port) {
        // Setup server
        testServer = new SasaServer();
        testServer.subscribe(this);
        testServer.listen(port);
        System.out.println("The Pilot is listening on port " + port);
    }

    public static void main(String[] args) {
        tpConnectionTest = new ConnectionTestTP(PORT);// Setup server to listen to a connecting Frog

        //User Interface TC
        UITP = new UserInterfaceTP();
        UITP.ConnectionTestInterface();
    }

    @Override
    public void receivedData(String message) {
        UITP.AddMessageToList(message);
        if(message.contains("gc-testconnection")) {
            tpConnectionTest.testServer.send("gc-testrecieved");
        }

    }

    @Override
    public void statusUpdate(String client, String status) {
        //TODO what to do when a client like The Frog (dis)connects)?
    }

}