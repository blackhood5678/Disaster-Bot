package nl.saxion.ptbc.ground_control.Views;

import nl.saxion.ptbc.ground_control.Models.DummyData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

//TODO code in offsets

public class CollisionMap extends JPanel
{
    private ArrayList<String> points = new ArrayList<>();

   public CollisionMap(ArrayList<String> points) {
       this.points = points.size() > 0 ? points : DummyData.collisionPoints;
   }

    public void paint(Graphics g){
        //I HATE COLLIUSION POINTS
        int x = 0;
        int y= 0;

        g.setColor(Color.green);
        g.fillRect(200, 340,  18, 16);
        for (String point:
                points) {
            String[] splitString = point.split(" ");
            x = (int)Double.parseDouble(splitString[2]);
            y = (int)Double.parseDouble(splitString[3]);
            if(x > 0) {
                x = x+100;
            } else {
                x = 100+x;
            }
            if(splitString[1].equals("POINT")) {
                g.setColor(Color.red);
                g.fillOval((x*3)-80, (100-y)*3, 8, 8);
            }

        }
    }

    public static void displayMap(ArrayList<String> points){
        JFrame f = new JFrame("Map");

        f.getContentPane().add(new CollisionMap(points));
        f.setSize(500, 400);
        f.setVisible(true);
        f.setResizable(false);
    }

}