package nl.saxion.ptbc.ground_control.Controllers;

import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;
import nl.saxion.ptbc.ground_control.Models.GroundControlDB;
import nl.saxion.ptbc.ground_control.Views.UserInterfaceGC;
import nl.saxion.ptbc.sockets.SasaClient;
import nl.saxion.ptbc.sockets.SasaConnectionException;
import nl.saxion.ptbc.sockets.SasaSocketInformation;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ground Control base class
 */
public class GroundControl implements SasaSocketInformation {
    private final SasaClient sasaClient;
    private static GroundControl groundControl;
    private static UserInterfaceGC UIGC;
    private static final int PORT = 50000;
    public static ArrayList<String> collisionPoints = new ArrayList<>();
    public static String currentPosition = "";


    public GroundControl(String host, int port) throws SasaConnectionException {
        // Setup server
        sasaClient = new SasaClient();
        sasaClient.subscribe(this);
        sasaClient.connect(host , port);
        System.out.println("Ground Control is connected to " + host + ":" + port);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            groundControl = new GroundControl("localhost", PORT); // Setup client to connect to ThePilot on this computer
            groundControl.sasaClient.send("Ground Control is now connected!");

            GroundControlDB db = new GroundControlDB();
            db.connect();//using this command u can make connection to the DB just add after connect the method you will need
        } catch (SasaConnectionException exception) {
            System.err.println("Connection error: " + exception.getMessage());
        }

        //User Interface GC
        UIGC = new UserInterfaceGC();
        UIGC.userInterfaceGC();
    }

    @Override
    public void receivedData(String message) {

        //Clean Mission log From GC before adding the new mission log entries
        if (message.startsWith("MLD")){
            try {
                GroundControlDB.deleteMissionLog();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // if a mission log row is sent from The Pilot, add this row to GroundControlDB
        if (message.startsWith("MLR ")) {
            String missionLogRow = message.replace("MLR ", "");
            String[] missionLogRowColumns = missionLogRow.split(";");
            String command = missionLogRowColumns[0];
            int map_id = Integer.parseInt(missionLogRowColumns[1]);

            try {
                GroundControlDB.addMissionLogRow(command, map_id);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        //Receiving current position and collision points for map
        if (message.startsWith("MAP RESPONSE:")) {
            currentPosition="";
            collisionPoints.clear();
            message = message.replace("MAP RESPONSE:", "");;
            String[] mapMessage= message.split(";");
            currentPosition=mapMessage[0];
            for (int i=1;i< mapMessage.length;i++) {
                collisionPoints.add(mapMessage[i]);
                try {
                    GroundControlDB.addCoordinate(mapMessage[i]);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void statusUpdate(String client, String status) {
        //TODO what to do when the server accepts (or disconnects)?
    }

    public static void clientSendMessage(String message) {
        groundControl.sasaClient.send(message);
    }
}
