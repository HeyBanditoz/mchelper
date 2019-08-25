package io.banditoz.mchelper.utils;

/**
 * Notchian 3D point (where y is up-and-down.)
 */
public class ThreeDimensionalPoint extends TwoDimensionalPoint {
    protected double y;

    public ThreeDimensionalPoint(double x, double y, double z) {
        super(x, z);
        this.y = y;
    }

    public ThreeDimensionalPoint(String x, String y, String z) {
        super(x, z);
        this.y = Double.parseDouble(y);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }
}
