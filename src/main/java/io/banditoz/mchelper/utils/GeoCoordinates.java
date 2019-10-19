package io.banditoz.mchelper.utils;

public class GeoCoordinates {
    private double latitude;
    private double longitude;

    public GeoCoordinates(double latitude, double longitude) {
        if ((latitude < -180 || latitude > 180) || (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Bad geographic coordinates!");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "GeoCoordinates{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
