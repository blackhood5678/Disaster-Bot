package nl.saxion.ptbc.ground_control.Views;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StyleUtils {
    public static void StyleButton(JButton toBeStyled) {
        toBeStyled.setBackground(new Color(80, 80, 127 ));
        toBeStyled.setForeground(new Color(232, 232, 232));
        toBeStyled.setFocusPainted(false);
        toBeStyled.setFont(new Font("Arial", Font.BOLD, 18));
        toBeStyled.setBorder(BorderFactory.createLineBorder(new Color(26, 24, 49), 5));
    }
    public static void StyleTextField(JTextField toBeStyled) {
        toBeStyled.setBackground(new Color(80, 80, 127 ));
        toBeStyled.setForeground(new Color(232, 232, 232));
        toBeStyled.setFont(new Font("Arial", Font.BOLD, 18));
        toBeStyled.setBorder(BorderFactory.createLineBorder(new Color(26, 24, 49), 5));
    }
    public static void StyleRadioButton(JRadioButton toBeStyled) {
        try{
            // Here MyOptionBtn1 is jRadioButton.
//            BufferedImage imgico = ImageIO.read(getClass().getResource("JBtn1.png"));
//            toBeStyled.setIcon(new ImageIcon(imgico));
//
//            BufferedImage imgicofocus = ImageIO.read(getClass().getResource("JBtn1_Focus.png"));
//            toBeStyled.setRolloverIcon(new ImageIcon(imgicofocus));
//            // setRolloverIcon is for when your cursor move on it or focus on it.
//
//            BufferedImage imgicoselect = ImageIO.read(getClass().getResource("JBtn1_select.png"));
//            toBeStyled.setSelectedIcon(new ImageIcon(imgicoselect));
//            // setSelectedIcon is for when you select this option.

        }catch(Exception ex1){}

    }
}
