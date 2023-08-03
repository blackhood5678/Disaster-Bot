package nl.saxion.ptbc.frog_pilot.Views;

import javax.swing.*;
import java.awt.*;

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
}
