
package nl.saxion.ptbc.ground_control.TestConnection;


import nl.saxion.ptbc.ground_control.Views.UserInterfaceGC;
import nl.saxion.ptbc.sockets.SasaClient;
import nl.saxion.ptbc.sockets.SasaConnectionException;
import nl.saxion.ptbc.sockets.SasaSocketInformation;

/**
 * ALWAYS RUN THIS SECOND */
public class ConnectionTestGC implements SasaSocketInformation {
    private final SasaClient testClient;
    private static ConnectionTestGC gcConnectionTest;
    private static TestUI UIGC;
    private static final int PORT = 50000;
    private static boolean loadUI = true;

    public ConnectionTestGC(String host, int port) throws SasaConnectionException {
        // Setup server  
        testClient = new SasaClient();
        testClient.subscribe(this);
        testClient.connect(host , port);
        System.out.println("Ground Control is connected to " + host + ":" + port);
    }

    public static void main(String[] args) {
        try {
            gcConnectionTest = new ConnectionTestGC("localhost", PORT); // Setup client to connect to ThePilot on this computer
            gcConnectionTest.testClient.send("Ground Control is now connected!");

        } catch (SasaConnectionException exception) {
            System.err.println("Connection error: " + exception.getMessage());
            loadUI = false;
        }

        //User Interface GC
        if(loadUI) {
            UIGC = new TestUI();
            UIGC.ConnectionTestInterface();
        }
    }

    public static void TestConnection() {
        gcConnectionTest.testClient.send("gc-testconnection");
    }

    @Override
    public void receivedData(String message) {
        //Tests if response is detected from the ground control.
        if(message.contains("gc-testrecieved")) {
            UIGC.TestRecieived(true);
        }


    }

    @Override
    public void statusUpdate(String client, String status) {
        //TODO what to do when the server accepts (or disconnects)?  
    }
}