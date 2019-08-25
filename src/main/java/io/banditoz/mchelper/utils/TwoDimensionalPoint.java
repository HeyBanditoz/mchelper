package io.banditoz.mchelper.utils;

/**
 * Notchian 2D point
 */
public class TwoDimensionalPoint {
    protected double x, z;

    public TwoDimensionalPoint(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public TwoDimensionalPoint(String x, String z) {
        this.x = Double.parseDouble(x);
        this.z = Double.parseDouble(z);
    }

    @Override
    public String toString() {
        return x + ", " + z;
    }

    public String toIntegerString() {
        int x = (int) Math.floor(this.x);
        int z = (int) Math.floor(this.z);

        return x + ", " + z;
    }

    public double getAngleBetweenTwoPoints(TwoDimensionalPoint tdp) {
        double angle = Math.toDegrees(Math.atan2((tdp.z - this.z), (tdp.x - this.x)));
        if (angle < -90) {
            return angle + 270;
        }
        else {
            return angle - 90;
        }
    }

    public double getDistance(TwoDimensionalPoint tdp) {
        double a = (this.x - tdp.x);
        double b = (this.z - tdp.z);

        return Math.sqrt((a*a) + (b*b));
    }

    public TwoDimensionalPoint getNetherCoordinates() {
        double x = this.x / 8;
        double z = this.z / 8;

        return new TwoDimensionalPoint(x, z);
    }

    public TwoDimensionalPoint getOverworldCoordinates() {
        double x = this.x * 8;
        double z = this.z * 8;

        return new TwoDimensionalPoint(x, z);
    }
}
