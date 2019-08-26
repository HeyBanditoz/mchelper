package io.banditoz.mchelper.utils;

import java.io.Serializable;

public class NamedCoordinatePoint extends TwoDimensionalPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;

    public NamedCoordinatePoint() {
    }

    public NamedCoordinatePoint(String name, double x, double z) {
        super(x, z);
        this.name = name;
    }

    public NamedCoordinatePoint(String name, String x, String z) {
        super(x, z);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": (" + x + ", " + z + ")";
    }
}
