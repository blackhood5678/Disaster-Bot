package nl.saxion.ptbc.ground_control.Models;

import nl.saxion.ptbc.ground_control.Controllers.GroundControl;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import static javax.swing.JOptionPane.showMessageDialog;

public class GroundControlDB {
    private static Connection connection; // USE THIS, DO NOT CREATE ANY NEW CONNECTION VARIABLE IN connect(). ELSE, SEND MISSION LOG TO GROUND CONTROL DB WON'T WORK.
    private final String url = "jdbc:postgresql://localhost/GroundControl";
    private final String user = "postgres";
    private final String password = "1234";

    /**
     * Connect to the PostgreSQL database
     *
     * @return a Connection object
     */
    public Connection connect() {
        connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the Ground Control database successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return connection;
    }


    public static ArrayList<String[]> exportMissions() throws SQLException {
        ArrayList<String[]> dataLines = new ArrayList<>();
        dataLines.add(new String[]{"mission_name", "command"});

        Statement s = connection.createStatement();
        s.execute("SELECT missions.mission_name, commands.command FROM missions LEFT JOIN commands ON missions.\"ID\" = commands.mission_id");
        ResultSet rs = s.getResultSet();

        while(rs.next()) {
            dataLines.add(new String[]{rs.getString(1), rs.getString(2)});
        }
        return dataLines;
    }

    public static void importMissions(ArrayList<Mission> missionsList) throws SQLException{
        for (Mission mission : missionsList) {
            String pkg = "MISSION:"+mission.name+";";
            String sql = "INSERT INTO missions (mission_name, map_id) VALUES ('"+mission.name+"', 1)";
            PreparedStatement s = connection.prepareStatement(sql);
            s.executeUpdate();


            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select \"ID\" from missions where mission_name = '"+mission.name+"'");
            result.next();
            int missionId = result.getInt(1);

            for (String string :
                    mission.commandsList) {
                 pkg = pkg + string + ";";
                sql = "INSERT into commands(command, mission_id) VALUES('"+string+"', "+missionId+")";
                s = connection.prepareStatement(sql);
                s.executeUpdate();
            }
            GroundControl.clientSendMessage(pkg);
        }

    }

    public static ArrayList<String[]> exportCollisions() throws SQLException {
        ArrayList<String[]> dataLines = new ArrayList<>();
        dataLines.add(new String[]{"type", "x", "y", "z"});

        Statement s = connection.createStatement();
        s.execute("SELECT coordinates.type, coordinates.x, coordinates.y, coordinates.z FROM coordinates WHERE coordinates.type = 'collision'");
        ResultSet rs = s.getResultSet();

        while(rs.next()) {
            dataLines.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
        }

        return dataLines;
    }

    public static void importCollisions(ArrayList<Collision> collisionsList) throws SQLException{
        String sql = "DELETE FROM coordinates WHERE type = 'collision'";
        PreparedStatement s = connection.prepareStatement(sql);
        s.executeUpdate();

        ArrayList<String> sqlCommands = new ArrayList<>();
        for (Collision collision :
                collisionsList) {
            sqlCommands.add("INSERT INTO coordinates(type,x,y,z)  VALUES ('collision',"+collision.x+", "+collision.y+", "+collision.z+")");
        }
        for (String sqlC :
                sqlCommands) {
            s = connection.prepareStatement(sqlC);
            int rowsInserted = s.executeUpdate();
        }
    }

    public static ResultSet readMissionLog() throws SQLException {
        String sql = "SELECT * FROM \"mission_log\"";
        Statement s = connection.createStatement();
        ResultSet result = s.executeQuery(sql);

        return result;
    }

    public static void addMissionLogRow(String command, int map_id) throws SQLException {
        String sql = "INSERT INTO mission_log (command, map_id) VALUES (?, ?)";
        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, command);
        s.setInt(2, map_id);

        int rowsInserted = s.executeUpdate();

        if (rowsInserted > 0) {
            System.out.println("Drive command added to database");
        }else {
            System.err.println("No drive command added");
        }
    }
    public static void deleteMissionLog() throws SQLException {
        String sql = "TRUNCATE TABLE mission_log RESTART IDENTITY"; // double quote needed for case-sensitive table/column names
        Statement s = connection.createStatement();

        int rowsDropped = s.executeUpdate(sql);

        if (rowsDropped == 0) {
            System.out.println("All rows dropped");
        } else {
            System.err.println("Not all rows dropped");
        }
    }
    public static void addMission(String missionName, ArrayList<String> commands) throws SQLException {
        int missionId = 0;

        //String sql = "INSERT into 'Missions'(mission_name, map_id) VALUES('" + missionName + "','" + 1 + "')";
        String sql = "INSERT INTO missions (mission_name, map_id) VALUES (?, 1)";

        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, missionName);

        if(!missionName.isEmpty() && !commands.isEmpty()) {
            int rowsInserted = s.executeUpdate();

            if (rowsInserted > 0) {
                String missionAdded = "Mission added to database";
                System.out.println(missionAdded + ": " + rowsInserted);
                showMessageDialog(null, missionAdded);
            }else {
                String missionNotAdded = "No mission added to database";
                System.err.println(missionNotAdded);
                showMessageDialog(null, missionNotAdded);
            }

            Statement s2 = connection.createStatement();
            s2.execute("select max(\"ID\") from missions");

            ResultSet rs = s2.getResultSet();
            if(rs.next()) {
                missionId = rs.getInt(1);

            } else {
                String missingMissionID = "Max missionID does not exist";
                System.out.println(missingMissionID);
                showMessageDialog(null, missingMissionID);

            }

            if(missionId > 0 && !commands.isEmpty()) {
                for (String command: commands) {

                        String Sql = "INSERT into commands(command, mission_id) VALUES(?, ?)";

                        s = connection.prepareStatement(Sql);
                        s.setString(1, command);
                        s.setInt(2, missionId);

                        int commandsInserted = s.executeUpdate();
                        System.out.println("commandsInserted: " + commandsInserted);

                }

            }


        }else if(commands.isEmpty() && missionName.isEmpty()){
            String noCommandOrMission = "No command or mission name entered.";
            System.out.println(noCommandOrMission);
            showMessageDialog(null, noCommandOrMission);

        }else if(missionName.isEmpty()) {
            String noMissionName = "No mission name entered.";
            System.out.println(noMissionName);
            showMessageDialog(null, noMissionName);

        }else if(commands.isEmpty()) {
            String noCommand = "No command entered.";
            System.out.println(noCommand);
            showMessageDialog(null, noCommand);

        }
    }

    public static void addCoordinate(String coordinate) throws SQLException {
        String[] dataLines = coordinate.split(" ");
        String sql = "INSERT into coordinates(type,x,y,z, map_id) VALUES('collision','"+dataLines[2]+"','"+dataLines[3]+"','"+dataLines[4]+"','"+1+"')";
        PreparedStatement s = connection.prepareStatement(sql);
        s.executeUpdate();
    }

    public static void addDriveToLocation(Double X, Double Y, Double Z, String endpoint) throws SQLException{
        String sql = "INSERT into coordinates(type,x,y,z, map_id) VALUES('"+endpoint+"','"+X+"','"+Y+"','"+Z+"','"+1+"')";
        PreparedStatement s = connection.prepareStatement(sql);
        s.executeUpdate();
        
    }
}