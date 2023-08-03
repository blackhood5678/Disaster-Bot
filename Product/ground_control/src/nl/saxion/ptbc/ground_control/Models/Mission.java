package nl.saxion.ptbc.ground_control.Models;

import java.util.ArrayList;

public class Mission {
    public String name;
    public ArrayList<String> commandsList = new ArrayList<>();

    @Override
    public String toString() {
        return "Mission{" +
                "name='" + name + '\'' +
                ", commandsList=" + commandsList +
                '}';
    }
}
