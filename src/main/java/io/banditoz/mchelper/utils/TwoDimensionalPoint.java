package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.annotation.*;

/**
 * Notchian 2D point
 */
public class TwoDimensionalPoint {
    @JsonProperty("x")
    private double x;
    @JsonProperty("z")
    private double z;

    @SuppressWarnings("unused") // prank! it's actually used by jackson!
    public TwoDimensionalPoint() {}

    public TwoDimensionalPoint(double x, double z) {
        this.x = x;
        this.z = z;
    }

    @JsonIgnore
    public TwoDimensionalPoint(String x, String z) {
        this.x = Double.parseDouble(x);
        this.z = Double.parseDouble(z);
    }

    @Override
    public String toString() {
        return x + ", " + z;
    }

    @JsonIgnore
    public String toIntegerString() {
        int x = (int) Math.floor(this.x);
        int z = (int) Math.floor(this.z);

        return x + ", " + z;
    }

    @JsonIgnore
    public double getAngleBetweenTwoPoints(TwoDimensionalPoint tdp) {
        double angle = Math.toDegrees(Math.atan2((tdp.z - this.z), (tdp.x - this.x)));
        if (angle < -90) {
            return angle + 270;
        }
        else {
            return angle - 90;
        }
    }

    @JsonIgnore
    public double getDistance(TwoDimensionalPoint tdp) {
        double a = (this.x - tdp.x);
        double b = (this.z - tdp.z);

        return Math.sqrt((a*a) + (b*b));
    }

    @JsonIgnore
    public TwoDimensionalPoint getNetherCoordinates() {
        double x = this.x / 8;
        double z = this.z / 8;

        return new TwoDimensionalPoint(x, z);
    }

    @JsonIgnore
    public TwoDimensionalPoint getOverworldCoordinates() {
        double x = this.x * 8;
        double z = this.z * 8;

        return new TwoDimensionalPoint(x, z);
    }


}
