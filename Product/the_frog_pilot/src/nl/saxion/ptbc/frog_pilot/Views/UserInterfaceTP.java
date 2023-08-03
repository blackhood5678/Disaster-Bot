package nl.saxion.ptbc.frog_pilot.Views;

import nl.saxion.ptbc.frog_pilot.Controllers.ThePilot;
import nl.saxion.ptbc.frog_pilot.Models.PilotDB;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.awt.*;
import java.sql.ResultSet;
import java.util.List;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class UserInterfaceTP {
    //TODO DEFINE ALL COMPONENTS OF UI HERE
    private JFrame mainMenuFrame;


    // Swing GUI components
    JButton sendMissionLogToGCDBButton;
    JButton refreshMissionLogButton;
    JPanel missionLogPanel;

    JLabel receivedData;
    String receivedMessage = "";

    JButton manualDrivingForward;
    JButton manualDrivingBackward;
    JButton manualDrivingFLeft;
    JButton manualDrivingFRight;
    JButton manualDrivingBLeft;
    JButton manualDrivingBRight;
    JButton manualDrivingBrake;

    JButton ppmButton;


    //MAIN METHOD TO DEFINE UI
    public void UserInterfaceTP() {
        //TODO MAIN USER INTERFACE GOES HERE
        mainMenuFrame = new JFrame("The Pilot");
        mainMenuFrame.setSize(600,600);
        mainMenuFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainMenuFrame.setLayout(null);

        // add mission log panel
        missionLogPanel = new JPanel();
        missionLogPanel.setBounds(280, 40, 280, 500);
        missionLogPanel.setBorder(BorderFactory.createTitledBorder("Mission Log - command_id, command, map_id")); // !!

        receivedData = new JLabel(); // THIS IS THE KEY COMPONENT FOR DISPLAYING THE MISSION LOG!!
        receivedData.setVerticalAlignment(JLabel.TOP);
        receivedData.setText("");
        missionLogPanel.add(receivedData, BorderLayout.CENTER);

        mainMenuFrame.add(missionLogPanel);

        refreshMissionLogButton = new JButton("Update Mission Log");
        refreshMissionLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMissionLogPanel();
                //JOptionPane.showMessageDialog(mainMenuFrame, "refreshMissionLogButton", "sent", 1); // for testing usage
            }
        });
        refreshMissionLogButton.setBounds(20, 50, 250, 60);
        mainMenuFrame.add(refreshMissionLogButton);

        sendMissionLogToGCDBButton = new JButton("Send Mission Log To GCDB");
        sendMissionLogToGCDBButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.serverSendMessage("MLD");
                try {
                    ResultSet ml = PilotDB.readMissionLog();
                    while (ml.next()) {
                        String missionLogRow = ml.getString("command") + ";" + ml.getInt("map_id");
                        ThePilot.serverSendMessage("MLR " + missionLogRow);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                JOptionPane.showMessageDialog(mainMenuFrame, "sendMissionLogToGCDBButton", "sent", 1); // for testing usage
            }
        });
        sendMissionLogToGCDBButton.setBounds(20, 120, 250, 60);
        mainMenuFrame.add(sendMissionLogToGCDBButton);

        // add manual driving components
        manualDrivingForward = new JButton("Forward");
        manualDrivingForward.setBounds(20, 200, 120, 60);
        manualDrivingForward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.ManualControls(ThePilot.ManualControlDirection.FORWARD);
            }
        });
        mainMenuFrame.add(manualDrivingForward);

        manualDrivingBackward = new JButton("Backward");
        manualDrivingBackward.setBounds(150, 200, 120, 60);
        manualDrivingBackward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.ManualControls(ThePilot.ManualControlDirection.BACKWARD);
            }
        });
        mainMenuFrame.add(manualDrivingBackward);

        manualDrivingFLeft = new JButton("FLeft");
        manualDrivingFLeft.setBounds(20, 270, 120, 60);
        manualDrivingFLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.ManualControls(ThePilot.ManualControlDirection.FLEFT);
            }
        });
        mainMenuFrame.add(manualDrivingFLeft);

        manualDrivingFRight = new JButton("FRight");
        manualDrivingFRight.setBounds(150, 270, 120, 60);
        manualDrivingFRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.ManualControls(ThePilot.ManualControlDirection.FRIGHT);
            }
        });
        mainMenuFrame.add(manualDrivingFRight);

        manualDrivingBLeft = new JButton("BLeft");
        manualDrivingBLeft.setBounds(20, 340, 120, 60);
        manualDrivingBLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.ManualControls(ThePilot.ManualControlDirection.BLEFT);
            }
        });
        mainMenuFrame.add(manualDrivingBLeft);

        manualDrivingBRight = new JButton("BRight");
        manualDrivingBRight.setBounds(150, 340, 120, 60);
        manualDrivingBRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.ManualControls(ThePilot.ManualControlDirection.BRIGHT);
            }
        });
        mainMenuFrame.add(manualDrivingBRight);

        manualDrivingBrake = new JButton("Brake");
        manualDrivingBrake.setBounds(20, 410, 250, 60);
        manualDrivingBrake.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThePilot.ManualControls(ThePilot.ManualControlDirection.BRAKE);
            }
        });
        mainMenuFrame.add(manualDrivingBrake);

        ppmButton = new JButton("Pre-programmed mission");
        ppmButton.setBounds(20, 480, 250, 60);
        ppmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PreProgrammedMissionDisplay();
            }
        });
        mainMenuFrame.add(ppmButton);

        mainMenuFrame.setVisible(true);

        displayMissionLog(); // display current mission log at startup

    }


    void addMessageToGUIMissionLog(String message) {
        receivedMessage = String.format("<div style=\"color: green;\"> %s</div>%s", message, receivedMessage);

        // Update UI
        receivedData.setText("<html>" + receivedMessage + "</html>");
        missionLogPanel.invalidate();

    }

    // retrieve the current commands in the mission_log of PilotDB and print it to the Mission Log JPanel of the UI
    private void displayMissionLog() {
        try {
            ResultSet ml = PilotDB.readMissionLog();
            while (ml.next()) {
                String GUIDisplayable = ml.getInt("id") + ";" + ml.getString("command")
                        + ";" + ml.getInt("map_id");
                addMessageToGUIMissionLog(GUIDisplayable);

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(mainMenuFrame, "Mission log displayed", "sent", 1); // for testing usage
    }

    // clear the current Mission Log panel and update it with the (possibly new) current mission log in the PilotDB
    private void updateMissionLogPanel() {
        // clear the Mission Log JPanel
        receivedData.setText("");
        receivedMessage = ""; // flush the old receivedMessage data (IMPORTANT!!!)

        displayMissionLog();
    }

    private class PreProgrammedMissionDisplay extends JFrame{
        private JFrame ppMainFrame;

        PreProgrammedMissionDisplay(){
            ppMainFrame = new JFrame("Pre-Programmed Missions");
            ppMainFrame.setSize(400, 400);
            ppMainFrame.setLayout(null);
            ppMainFrame.setVisible(true);

            DefaultListModel missionListModel = new DefaultListModel();
            try{
                List<String> missionNames = PilotDB.getAllMissionNames();
                String[] names = new String[missionNames.size()];
                for(int i = 0;i<missionNames.size();i++){
                    names[i] = missionNames.get(i);
                    missionListModel.addElement(names[i]);
                }
            }catch (RuntimeException re){
                JOptionPane.showMessageDialog(ppMainFrame, re.getMessage());
            }

            JLabel missionLabel = new JLabel("Selected Mission");
            JTextField selectedMission = new JTextField();
            JButton driveButton = new JButton("Drive");
            JPanel missionPanel = new JPanel();
            JList missionList = new JList<>(missionListModel);

            missionLabel.setBounds(200,30,150,27);
            selectedMission.setBounds(200,50,170,35);
            driveButton.setBounds(200,100,170,30);
            missionPanel.setBounds(20, 20, 150, 320);

            selectedMission.setEditable(false);
            missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            missionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            missionList.setVisibleRowCount(-1);

            driveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        ThePilot.executeMission(missionList.getSelectedValue().toString());
                    }catch (RuntimeException re){
                        JOptionPane.showMessageDialog(ppMainFrame, re.getMessage());
                    }
                }
            });

            missionList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    selectedMission.setText(missionList.getSelectedValue().toString());
                }
            });

            missionPanel.add(missionList);
            ppMainFrame.add(missionLabel);
            ppMainFrame.add(missionPanel);
            ppMainFrame.add(driveButton);
            ppMainFrame.add(selectedMission);
        }
    }

}
