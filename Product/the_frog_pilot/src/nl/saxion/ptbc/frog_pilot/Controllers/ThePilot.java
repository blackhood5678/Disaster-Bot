package nl.saxion.ptbc.frog_pilot.Controllers;

import nl.saxion.ptbc.frog_pilot.Classes.Coordinate;
import nl.saxion.ptbc.frog_pilot.Models.PilotDB;
import nl.saxion.ptbc.frog_pilot.Views.UserInterfaceTP;
import nl.saxion.ptbc.sockets.SasaServer;
import nl.saxion.ptbc.sockets.SasaSocketInformation;
import com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The base class of The Pilot.
 */
public class ThePilot implements SasaSocketInformation {
    private final SasaServer sasaServer;
    private static ThePilot thePilot;
    private final PilotDB pilotDB;
    private static final int PORT = 50000;
    private static UserInterfaceTP UITP;
    private static AutomaticDriving automaticDriving;
    public static ArrayList<String> collisionPoints = new ArrayList<>();
    public static String currentPosition = "";


    public static enum ManualControlDirection {
        FORWARD,
        BACKWARD,
        FLEFT,
        FRIGHT,
        BLEFT,
        BRIGHT,
        BRAKE
    }

    //=========================================================================
    public ThePilot(int port) {
        // Setup server
        sasaServer = new SasaServer();
        sasaServer.subscribe(this);
        sasaServer.listen(port);
        System.out.println("The Pilot is listening on port " + port);
        this.automaticDriving = new AutomaticDriving();
        // Setup database manager
        pilotDB = new PilotDB();
        pilotDB.connect();
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatSpacegrayIJTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
        thePilot = new ThePilot(PORT);

        // start the Frog
        String path = null;
        String command = null;
        try {
            path = System.getProperty("user.dir");
            String relativePath = "\\TheFrogWinV2022-6.X64\\";
            String relativeCommand = "\\TheFrogWinV2022-6.X64\\TheFrog.exe";
            command = path + relativeCommand;
            path += relativePath;


            Runtime.getRuntime().exec(command,
                    null, new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(path);
            System.out.println(command);
        }

        //User Interface TC
        UITP = new UserInterfaceTP();
        UITP.UserInterfaceTP();

    }
    @Override
    public void receivedData(String message) {

        //Automatic driving coordinates send from GC
        if (message.startsWith("AUTODRIVE")){
            String autodriveCoordinate = message.replace("AUTODRIVE", "");
            String[] autodriveCoordinateParts = autodriveCoordinate.split(" ");

            double x = Double.parseDouble(autodriveCoordinateParts[0]);
            double y = Double.parseDouble(autodriveCoordinateParts[1]);
            double z = Double.parseDouble(autodriveCoordinateParts[2]);

            try {
                pilotDB.addDriveToLocation(x, y, z, "endPoint");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Coordinate GOAL_POINT = new Coordinate(x,y,z,"endPoint");
            automaticDriving.Auto(GOAL_POINT);
        }

        //Receiving collision points from rover
        if (message.startsWith("FROG POINT")){
            collisionPoints.add(message.replace(",", "."));

            String obstacleCoordinate = message.replace("FROG POINT ", "");
            obstacleCoordinate = obstacleCoordinate.replace(",", ".");
            String[] obstacleCoordinateParts = obstacleCoordinate.split(" ");

            double x = Double.parseDouble(obstacleCoordinateParts[0])+automaticDriving.getCurrentPosition().getX();
            double y = Double.parseDouble(obstacleCoordinateParts[1])+automaticDriving.getCurrentPosition().getY();
            double z = Double.parseDouble(obstacleCoordinateParts[2]);

            Coordinate coordinate = new Coordinate(x,y,z,"collision");
            automaticDriving.setCollision(coordinate);

        }

        //Receiving rover current position
        if (message.startsWith("FROG POSITION")){
            currentPosition = message.replace(",", ".");
            collisionPoints.clear();
            collisionPoints = new ArrayList<>();

            String obstacleCoordinate = message.replace("FROG POSITION ", "");
            obstacleCoordinate = obstacleCoordinate.replace(",", ".");
            String[] obstacleCoordinateParts = obstacleCoordinate.split(" ");
            double x = Double.parseDouble(obstacleCoordinateParts[0]);
            double y = Double.parseDouble(obstacleCoordinateParts[1]);
            double z = Double.parseDouble(obstacleCoordinateParts[2]);
            if(z > 180)
                z = z-360;
            automaticDriving.setOldPosition();
            automaticDriving.setCurrentPosition(new Coordinate(x,y,z,"location"));
            automaticDriving.collisionsClear();
        }

        //Receiving mission from GC & adding mission to DB
        if (message.startsWith("MISSION:")){
            message = message.replace("MISSION:", "");
            String[] mission = message.split(";");
            String missionName=mission[0];
            List<String>commands=new ArrayList<>();
            for (int i=1;i< mission.length;i++){
                commands.add(mission[i]);
            }
            try {
                pilotDB.addMission(missionName, commands);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //Sending Data to GC for the map on request
            if (message.startsWith("MAP REQUEST")){
            String mapMessage= "MAP RESPONSE:"+currentPosition;
            for(int i=0;i<collisionPoints.size();i++){
                mapMessage=mapMessage+";"+collisionPoints.get(i);
            }
            ThePilot.serverSendMessage(mapMessage);
        }
    }
    @Override
    public void statusUpdate(String client, String status) {
        //TODO what to do when a client like The Frog (dis)connects)?
    }

    // HH: Changed this to static, necessary for ActionListeners of the UI. Also changed the commands, I think it works now.
    public static void ManualControls(ManualControlDirection d) {
        String dc = "";
        if (d == ManualControlDirection.FORWARD) {
            dc = "PILOT DRIVE 1000 0 0.25";

        }
        else if (d == ManualControlDirection.BACKWARD) {
            dc = "PILOT DRIVE -1000 0 0.25";
        }
        else if (d == ManualControlDirection.FRIGHT) {
            dc = "PILOT DRIVE 1000 45 0.25";
        }
        else if (d == ManualControlDirection.FLEFT) {
            dc = "PILOT DRIVE 1000 -45 0.25";
        }
        else if (d == ManualControlDirection.BRIGHT) {
            dc = "PILOT DRIVE -1000 45 0.25";
        }
        else if (d == ManualControlDirection.BLEFT) {
            dc = "PILOT DRIVE -1000 -45 0.25";
        }
        else if (d == ManualControlDirection.BRAKE) {
            dc = "PILOT DRIVE 0 0 0";
        }

        thePilot.sasaServer.send(dc);
        addDriveCommandToPilotDBMissionLog(dc);
    }

    // Used to deal with the fact that there is no sasaServer in UITP. This is stupid.
    public static void serverSendMessage(String message) {
        thePilot.sasaServer.send(message);
    }

    // add a drive command to the mission_log table of The Pilot database. This is created to avoid redundant try/catches.
    public static void addDriveCommandToPilotDBMissionLog(String message) {
        try {
            PilotDB.updateMissionLog(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Will execute list of commands that is fetched from the database
     * that is associated with the provided missionName through the parameter
     * @param missionName
     */
    public static void executeMission(String missionName){
        try{
            List<String> commands = PilotDB.getMissionCommands(missionName);
            if(!commands.isEmpty()){
                for(String s:commands){
                    String[] duration = s.split(" ");
                    ThePilot.serverSendMessage(s);
                    TimeUnit.SECONDS.sleep(Integer.parseInt(duration[4]));
                }
            }else{
                throw new RuntimeException("No commands where found!");
            }
        }catch (RuntimeException re){
            throw new RuntimeException(re.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
