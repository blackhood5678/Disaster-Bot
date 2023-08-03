package nl.saxion.ptbc.ground_control.Models;

public class Collision {
    public int x;
    public int y;
    public int z;

    public Collision(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "Collision{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
