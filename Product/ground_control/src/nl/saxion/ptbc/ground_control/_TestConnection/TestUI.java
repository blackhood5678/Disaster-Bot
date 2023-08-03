package nl.saxion.ptbc.ground_control.TestConnection;

import nl.saxion.ptbc.ground_control.Views.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestUI {
    //TODO DEFINE ALL COMPONENTS OF UI HERE
    JButton testConnectionButton;
    JTextField testRecieved;
    JFrame mainMenuFrame;

    public void ConnectionTestInterface() {
        testConnectionButton = new JButton("Test Connection");
        testConnectionButton.setBounds(100,100,200, 40);
        StyleUtils.StyleButton(testConnectionButton);

        testRecieved = new JTextField("");
        testRecieved.setBounds(100,150,200, 40);
        StyleUtils.StyleTextField(testRecieved);

        mainMenuFrame = new JFrame("Connection Test GC");
        mainMenuFrame.add(testRecieved);
        mainMenuFrame.add(testConnectionButton);
        mainMenuFrame.setSize(400,500);
        mainMenuFrame.getContentPane().setBackground( new Color(251, 248, 231) );
        mainMenuFrame.setLayout(null);
        mainMenuFrame.setVisible(true);

        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConnectionTestGC.TestConnection();
            };
        });
    }

    public void TestRecieived(boolean connection) {
        //Updates UI when response is recieved
        if (connection) {
            testRecieved.setText("Connection Complete!");
        } else {
            testRecieved.setText("No Connection!");
        }

    }

}

