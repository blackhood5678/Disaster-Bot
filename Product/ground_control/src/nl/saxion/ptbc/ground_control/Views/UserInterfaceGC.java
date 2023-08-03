package nl.saxion.ptbc.ground_control.Views;
import nl.saxion.ptbc.ground_control.Controllers.GroundControl;
import nl.saxion.ptbc.ground_control.Models.Collision;
import nl.saxion.ptbc.ground_control.Models.DummyData;
import nl.saxion.ptbc.ground_control.Models.GroundControlDB;
import nl.saxion.ptbc.ground_control.Models.Mission;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static javax.swing.JOptionPane.showMessageDialog;

public class UserInterfaceGC {
    //TODO DEFINE ALL COMPONENTS OF UI HERE
    private JFrame mainJFrame;
    private JButton driveToLocation;
    private JButton createMission;
    private JButton displayMap;
    private JButton exportData;
    private JButton importData;
    private JRadioButton collisions;
    private JRadioButton missions;
    private JButton newCommand;

    // mission log components
    JButton refreshMissionLogButton;
    JPanel missionLogPanel;
    JLabel receivedData;
    String receivedMessage = "";

    private GroundControlDB GCDB = new GroundControlDB();

    //MAIN METHOD TO DEFINE UI
    public void userInterfaceGC() {
        //TODO MAIN USER INTERFACE GOES HERE

        //Initializing main menu attributes
        initializeMainJFrame();

        //Defining Buttons
        driveToLocation = new JButton("Drive to Location");
        driveToLocation.setBounds(20, 50, 200, 40);

        createMission = new JButton("Create Missions");
        createMission.setBounds(20, 100, 200, 40);
        displayMap = new JButton("Display Map");
        displayMap.setBounds(20, 150, 200, 40);

        exportData = new JButton("Export");
        exportData.setBounds(20, 250, 200, 40);

        importData = new JButton("Import");
        importData.setBounds(20, 300, 200, 40);

        collisions = new JRadioButton("Collisions");
        missions = new JRadioButton("Missions");
        collisions.setBounds(20,200,100,30);
        missions.setBounds(150,200,100,30);
        ButtonGroup bg=new ButtonGroup();
        collisions.setSelected(true);
        bg.add(collisions);bg.add(missions);

        //Adding Components
        addComponentToMain(driveToLocation);
        addComponentToMain(createMission);
        addComponentToMain(displayMap);
        addComponentToMain(collisions);
        addComponentToMain(missions);
        addComponentToMain(exportData);
        addComponentToMain(importData);

        driveToLocation.addActionListener(new DriveToLocationAction());
        createMission.addActionListener(new CreateMissionAction());
        displayMap.addActionListener(new DisplayMapAction());
        importData.addActionListener(new ImportDataAction());
        exportData.addActionListener(new ExportDataAction());

        // define and add mission log to the main frame
        // add mission log panel
        missionLogPanel = new JPanel();
        missionLogPanel.setBounds(240, 40, 260, 350);
        missionLogPanel.setBorder(BorderFactory.createTitledBorder("Mission Log - command_id, command, map_id")); // !!

        receivedData = new JLabel(); // THIS IS THE KEY COMPONENT FOR DISPLAYING THE MISSION LOG!!
        receivedData.setVerticalAlignment(JLabel.TOP);
        receivedData.setText("");
        missionLogPanel.add(receivedData, BorderLayout.CENTER);

        addComponentToMain(missionLogPanel);

        // add refresh button
        refreshMissionLogButton = new JButton("Update Mission Log");
        refreshMissionLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMissionLogPanel();
            }
        });
        refreshMissionLogButton.setBounds(20, 350, 200, 40);
        addComponentToMain(refreshMissionLogButton);

        displayMissionLog();


    }

    //ALL ACTION LISTENERS.
    class DriveToLocationAction implements ActionListener {
        public void actionPerformed(ActionEvent e) { //Get the button name using getAction Command
            new DriveFrame();
        }
    }

    class CreateMissionAction implements ActionListener {
        public void actionPerformed(ActionEvent e) { //Get the button name using getAction Command
            new CreateMissionFrame();
        }
    }

    class DisplayMapAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            GroundControl.clientSendMessage("MAP REQUEST");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            CollisionMap.displayMap(GroundControl.collisionPoints);
            PositionMap.displayMap(GroundControl.currentPosition);
        }
    }

    class ImportDataAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("*.csv", "csv"));
            File file = null;

            int returnVal = fc.showOpenDialog(new JFrame());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " + fc.getSelectedFile().getName());
                file = fc.getSelectedFile();
            }

            if(collisions.isSelected() && file != null) {
                //import collisions
                ArrayList<Collision> collisionsList = new ArrayList<>();
                String line = "";
                String splitBy = ";";
                try
                {
                    //parsing a CSV file into BufferedReader class constructor
                    BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
                    br.readLine();
                    while ((line = br.readLine()) != null)   //returns a Boolean value
                    {
                        String[] data = line.split(splitBy);    // use comma as separator
                        Collision collision = new Collision(Integer.parseInt(data[0]),Integer.parseInt(data[1]),Integer.parseInt(data[2]));
                        collisionsList.add(collision);
                    }
                }
                catch (IOException ei)
                {
                    ei.printStackTrace();
                }

                try {
                    GroundControlDB.importCollisions(collisionsList);
                } catch (SQLException ei) {
                    System.out.println(ei);
                }

            } else if(file != null) {
                //import missions
                ArrayList<Mission> missionsList = new ArrayList<>();
                Mission mission = new Mission();
                String line = "";
                String splitBy = ";";
                int count = 0;
                try
                {
                    //parsing a CSV file into BufferedReader class constructor
                    BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));

                    //Skip header and head into the loop
                    br.readLine();

                    while ((line = br.readLine()) != null)
                    {
                        String[] data = line.split(splitBy);
                        mission.name = data[0];

                        if(missionsList.isEmpty()) {
                            missionsList.add(mission);
                            mission = new Mission();
                        }
                        if (!data[0].equals(missionsList.get(count).name)) {
                            count++;
                            missionsList.add(mission);
                            mission = new Mission();
                        }
                        missionsList.get(count).commandsList.add(data[1]);


                    }
                }
                catch (IOException ei)
                {
                    ei.printStackTrace();
                }

                try {
                    GroundControlDB.importMissions(missionsList);
                } catch (SQLException ei) {
                    System.out.println(ei);
                }
            }
        }
    }

    class ExportDataAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ArrayList<String[]> dataLines = new ArrayList<>();
            if(collisions.isSelected()) {
                //export collisions
                try {
                    dataLines = GroundControlDB.exportCollisions();
                } catch (SQLException ei) {
                    System.out.println(ei);
                }

            } else {
                //export missions
                try {
                    dataLines = GroundControlDB.exportMissions();
                } catch (SQLException ei) {
                    System.out.println(ei);
                }

            }
            try {
                createCSV(dataLines);
            } catch (IOException ie) {
                System.out.println("could not export!");
            }
        }

        public void createCSV(ArrayList<String[]> dataLines)  throws IOException {
            File csvOutputFile;
            //Please make sure the directory specified exists
            if(collisions.isSelected()) {
                //export collisions
                 csvOutputFile = new File("shared/src/nl/saxion/ptbc/shared/exports/collisions.csv");
            } else {
                //export missions
                csvOutputFile = new File("shared/src/nl/saxion/ptbc/shared/exports/missions.csv");
            }

            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                dataLines.stream()
                        .map(this::convertToCSV)
                        .forEach(pw::println);
            }
        }

        public String convertToCSV(String[] data) {
            return Stream.of(data)
                    .map(this::escapeSpecialCharacters)
                    .collect(Collectors.joining(","));
        }

        public String escapeSpecialCharacters(String data) {
            String escapedData = data.replaceAll("\\R", " ");
            if (data.contains(",") || data.contains("\"") || data.contains("'")) {
                data = data.replace("\"", "\"\"");
                escapedData = "\"" + data + "\"";
            }
            return escapedData;
        }

    }

    //IF CREATING A NEW FRAME WITHIN AN ACTION LISTENER, USE THE FOLLOWING EXAMPLE
    //ALL CLASSES
    private class DriveFrame extends JFrame
    {
        JTextField X, Y, Z;
        JButton submitLocation;

        // constructor
        DriveFrame()
        {
            setTitle("Drive to Location");
            setSize(400, 400);
            setLayout(null);
            setVisible(true);

            X = new JTextField("Enter X coordinate");
            X.setBounds(50, 100, 200, 30);
            Y = new JTextField("Enter Y coordinate");
            Y.setBounds(50, 150, 200, 30);

            Z = new JTextField("Enter Z coordinate");
            Z.setBounds(50, 200, 200, 30);

            submitLocation = new JButton("Submit");
            submitLocation.setBounds(100, 250, 100, 40);

            add(X);
            add(Y);
            add(Z);
            add(submitLocation);

            submitLocation.addActionListener(new SubmitLocationAction());
        }

        class SubmitLocationAction implements ActionListener {
            public void actionPerformed(ActionEvent e) {

                try{
                    double x = Double.parseDouble(X.getText());
                    double y = Double.parseDouble(Y.getText());
                    double z = Double.parseDouble(Z.getText());

                    String message = "AUTODRIVE" +x+" " +y+" " +z;
                    GroundControl.clientSendMessage(message);

                    try {
                        GCDB.addDriveToLocation(x, y, z, "endPoint");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    String coordinatesAdded = "Coordinates added succesfully. ";
                    System.out.println(coordinatesAdded);
                    showMessageDialog(null, coordinatesAdded);

                }catch(NumberFormatException nn){
                    String numericalValue = "X, Y, Z must have numerical value.";
                    System.out.println(numericalValue);
                    showMessageDialog(null, numericalValue );
                }

                DriveFrame.this.dispose();

            }
        }

    }

    private class CreateMissionFrame extends JFrame
    {
        JTextField missionName, commandLine;
        JComboBox<String> mapSelect;
        JButton saveMission, addCommand;
        JList missionActionsList;
        DefaultListModel<String> commandList = new DefaultListModel<String>();

        // constructor
        CreateMissionFrame()
        {
            setTitle("Create Mission");
            setSize(500, 500);
            setLayout(null);
            setVisible(true);

            //new Mission name
            missionName = new JTextField();
            missionName.setBounds(250, 50, 200, 40);

            commandLine = new JTextField();
            commandLine.setBounds(250, 100, 200, 40);

            addCommand = new JButton("Add Command");
            addCommand.setBounds(250, 350, 200, 40);

            saveMission = new JButton("Save");
            saveMission.setBounds(250, 400, 200, 40);

            missionActionsList = new JList(commandList);
            missionActionsList.setBounds(20, 50, 200, 400);

            String[] optionsToChoose = {"Map 1","N/A","N/A","N/A"};
            mapSelect = new JComboBox(optionsToChoose);

            add(commandLine);
            add(addCommand);
            add(mapSelect);
            add(missionName);
            add(saveMission);
            add(missionActionsList);


            addCommand.addActionListener(new AddMissionLineAction());
            saveMission.addActionListener(new SubmitMissionAction());
        }

        class AddMissionLineAction implements ActionListener {
            public void actionPerformed(ActionEvent e) { //Get the button name using getAction Command
                if (!commandLine.getText().equalsIgnoreCase("") &&  commandLine.getText().toUpperCase(Locale.ROOT).startsWith("PILOT DRIVE ")) {

                    String str = commandLine.getText();
                    String[] split = str.split(" ", 5);

                    if(split.length == 5) {
                        try {
                            int motorPower = Integer.parseInt(split[2]);
                            int driveAngle = Integer.parseInt(split[3]);
                            int driveDuration = Integer.parseInt(split[4]);

                            commandList.addElement(commandLine.getText().toUpperCase(Locale.ROOT));
                        } catch (NumberFormatException ni) {
                            String notIntegers = "Values entered are not integers.";
                            System.out.println(notIntegers);

                            showMessageDialog(null, notIntegers);
                        }


                    }else{
                        String incompleteCommand = "The command entered is incomplete.";

                        System.out.println(incompleteCommand);
                        showMessageDialog(null, incompleteCommand);

                    }


                }else{
                    String commandFormat = "Command format must be: PILOT DRIVE int int int ";
                    System.out.println(commandFormat);
                    showMessageDialog(null, commandFormat);
                }
                commandLine.setText("");
            }
        }

        class SubmitMissionAction implements ActionListener {
            public void actionPerformed(ActionEvent e) { //Get the button name using getAction Command
                ArrayList<String> list = new ArrayList<>();
                for (Object command: commandList.toArray()) {
                    list.add(command.toString());
                }
                String missionSend = "MISSION:"+missionName.getText();
                for (String command: list) {
                    missionSend=missionSend+";"+command;
                }
                GroundControl.clientSendMessage(missionSend);

                try {
                    GroundControlDB.addMission(missionName.getText(), list);
                    missionName.setText("");
                } catch(SQLException se) {
                    se.printStackTrace();
                }

                CreateMissionFrame.this.dispose();
            }
        }
    }

    // HELP FUNCTIONS BELOW
    public void initializeMainJFrame() {
        mainJFrame = new JFrame("The Ground Control");
        mainJFrame.setSize(550, 450);
        mainJFrame.setLayout(null);
        mainJFrame.setVisible(true);
    }

    public void addComponentToMain(JComponent component) {
        mainJFrame.add(component);
    }

    public void addComponentToFrame(JFrame frame, JComponent component) {
        frame.add(component);
    }

    private void displayMissionLog() {
        try {
            ResultSet ml = GroundControlDB.readMissionLog();
            while (ml.next()) {
                String GUIDisplayable = ml.getInt("id") + ";" + ml.getString("command")
                        + ";" + ml.getInt("map_id");
                addMessageToGUIMissionLog(GUIDisplayable);

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(mainJFrame, "Mission log displayed", "sent", 1); // for testing usage
    }

    // clear the current Mission Log panel and update it with the (possibly new) current mission log in the GroundControlDB
    private void updateMissionLogPanel() {
        // clear the Mission Log JPanel
        receivedData.setText("");
        receivedMessage = ""; // flush the old receivedMessage data (IMPORTANT!!!)

        displayMissionLog();
    }

    private void addMessageToGUIMissionLog(String message) {
        receivedMessage = String.format("<div style=\"color: green;\"> %s</div>%s", message, receivedMessage);

        // Update UI
        receivedData.setText("<html>" + receivedMessage + "</html>");
        missionLogPanel.invalidate();

    }

}