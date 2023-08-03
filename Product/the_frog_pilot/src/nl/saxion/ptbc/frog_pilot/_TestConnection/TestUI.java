package nl.saxion.ptbc.frog_pilot.TestConnection;

import javax.swing.*;

public class TestUI {
    private static JList logList;
    private DefaultListModel<String> messagesList = new DefaultListModel<String>();

    public void ConnectionTestInterface() {
        mainMenuFrame = new JFrame("Connection Test TP");
        logList = new JList(messagesList);
        logList.setBounds(100,100,200, 400);

        mainMenuFrame.add(logList);
        mainMenuFrame.setSize(400,500);
        mainMenuFrame.setLayout(null);
        mainMenuFrame.setVisible(true);
    }

    public void AddMessageToList(String message) {
        //adds logged message to the item.
        messagesList.addElement(message);
    }
}
