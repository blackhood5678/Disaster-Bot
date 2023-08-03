package nl.saxion.ptbc.ground_control.Views;
import nl.saxion.ptbc.ground_control.Models.DummyData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PositionMap {
    private static  FrogPosition frogPosition;
    private static ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public static void displayMap(String currentPosition) {
        JFrame frame = new JFrame("Position");

        JRadioButton changeButton = new JRadioButton("SHOW FROM POSITION");
        changeButton.setSelected(false);

        frame.add(changeButton, BorderLayout.SOUTH);
        frame.add(new JLabel(new ImageIcon("ground_control/src/nl/saxion/ptbc/ground_control/Views/Assets/map.PNG")), BorderLayout.CENTER);

        frogPosition = new FrogPosition(currentPosition);
        changeButton.addItemListener(frogPosition);
        changeButton.setVisible(false);
        frame.setGlassPane(frogPosition);


        ScheduledExecutorService executorService;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                changeButton.doClick();
            }
        }, 0, 1, TimeUnit.SECONDS);

        //Show the window.
        frame.pack();
        frame.setVisible(true);
    }

}



/**
 * We have to provide our own glass pane so that it can paint.
 */
class FrogPosition extends JComponent implements ItemListener {
    private String position;
    public FrogPosition(String position) {
        this.position = position.isBlank() ? DummyData.currentPosition : position;
    }

    public void itemStateChanged(ItemEvent e) {
        setVisible(e.getStateChange() == ItemEvent.SELECTED);
    }

    protected void paintComponent(Graphics g) {
        String[] splitString = position.split(" ");
        int y = ((int) Double.parseDouble(splitString[2]));
        int x = ((int) Double.parseDouble(splitString[3]));
        if (y < 0) {
            y = 438 + (-1 * y);
        } else {
            y = 438 - y;
        }
        if (x < 0) {
            x = 345 + (-1 * x);
        } else {
            x = 345 - x;
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1));
        g2d.setColor(Color.green);
        g2d.fillOval(x, y, 12, 12);
    }

}
