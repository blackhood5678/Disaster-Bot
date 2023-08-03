package nl.saxion.ptbc.frog_pilot.Classes;

public class Coordinate {
    private Long ID;
    private double x;
    private double y;
    private double z;
    private String type;
    private int mapID;

    public Coordinate(double x, double y, double z, String type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.mapID = 1;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getType() {
        return type;
    }

    public int getMapID() {
        return mapID;
    }

    @Override
    public String toString() {
        return x+" "+y+" "+z;
    }
}
