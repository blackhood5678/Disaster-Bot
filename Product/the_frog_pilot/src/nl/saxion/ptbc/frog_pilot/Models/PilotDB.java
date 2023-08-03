package nl.saxion.ptbc.frog_pilot.Models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;

public class PilotDB {
    private static Connection connection; // USE THIS, DO NOT CREATE ANY NEW CONNECTION VARIABLE IN connect(). ELSE, THE PILOT UI WILL BREAK.
    private final String url = "jdbc:postgresql://localhost/Pilot";
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
            System.out.println("Connected to the Pilot database successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return connection;
    }

    // CRUD naming
    public static ResultSet readMissionLog() throws SQLException {
        String sql = "SELECT * FROM \"mission_log\"";
        Statement s = connection.createStatement();
        ResultSet result = s.executeQuery(sql);

        return result;
    }

    // save a drive command to the mission_log table of PilotDB
    // Precondition: map_id = 1 exists in the 'maps' table
    public static void updateMissionLog(String command) throws SQLException {
        String sql = "INSERT INTO mission_log (command, map_id) VALUES (?, 1)";

        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, command);

        int rowsInserted = s.executeUpdate();

        if (rowsInserted > 0) {
            System.out.println("Drive command added to database");
        } else {
            System.err.println("No drive command added");
        }
    }

    public static void addDriveToLocation(Double X, Double Y, Double Z, String endpoint) throws SQLException {

        String sql = "INSERT into coordinates(type,x,y,z, map_id) VALUES('" + endpoint + "','" + X + "','" + Y + "','" + Z + "','" + 1 + "')";
        PreparedStatement s = connection.prepareStatement(sql);
        s.executeUpdate();

    }

    public static void addMission(String missionName, List<String> commands) throws SQLException {

        String sql = "INSERT INTO missions (mission_name, map_id) VALUES ('" + missionName + "', 1)";
        System.out.println(sql);
        PreparedStatement s = connection.prepareStatement(sql);
        s.executeUpdate();


        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("select \"ID\" from missions where mission_name = '" + missionName + "'");
        result.next();
        int missionId = result.getInt(1);

        for (String string : commands) {
            sql = "INSERT into commands(command, mission_id) VALUES('" + string + "', " + missionId + ")";
            System.out.println(sql);
            s = connection.prepareStatement(sql);
            s.executeUpdate();
        }
    }
    /**
     * Will take missionName as paramater and get commands linked to the mission
     * and return ArrayList of the commands to be executed
     * @param missionName
     * @return commands
     */
    public static List<String> getMissionCommands(String missionName) {
        List<String> commands = new ArrayList<>();
        String sqlQuery = "SELECT c.command FROM missions m INNER JOIN commands c ON c.mission_id = m.\"ID\" WHERE m.mission_name = " + "'" + missionName + "' order by c.id";
        try {
            ResultSet result = connection.createStatement().executeQuery(sqlQuery);
            while (result.next()) {
                String command = result.getString(1);
                String[] dbCommand = command.split(" ");
                if (dbCommand[0].equalsIgnoreCase("PILOT") && dbCommand[1].equalsIgnoreCase("DRIVE") && dbCommand.length == 5) {
                    String valiadedCommand = "";
                    for (int i = 0; i < dbCommand.length; i++) {
                        if (i == 0 || i == 1) {
                            valiadedCommand += dbCommand[i].toUpperCase() + " ";
                        } else {
                            valiadedCommand += dbCommand[i] + " ";
                        }
                    }
                    commands.add(valiadedCommand);
                } else {
                    throw new RuntimeException("Invalid command: " + command);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return commands;
    }

    /**
     * Will return list of all mission names from the database
     * @return List
     */

    public static List<String> getAllMissionNames(){
        List<String> missionNames = new ArrayList<>();
        String sqlQuery = "SELECT mission_name FROM missions";
        try {
            ResultSet result = connection.createStatement().executeQuery(sqlQuery);
            while(result.next()){
                missionNames.add(result.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return missionNames;
    }
}
