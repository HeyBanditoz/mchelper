package io.banditoz.mchelper.utils.weather;

import java.math.BigDecimal;

public class GeoCoordinates {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private static BigDecimal MIN = new BigDecimal("-180");
    private static BigDecimal MAX = new BigDecimal("180");

    public GeoCoordinates(String latitude, String longitude) {
        this.latitude = new BigDecimal(latitude);
        this.longitude = new BigDecimal(longitude);
        if (((this.latitude.compareTo(MIN) < 0) || (this.latitude.compareTo(MAX) > 0)) || (this.longitude.compareTo(MIN) < 0) || (this.longitude.compareTo(MAX) > 0)) {
            throw new IllegalArgumentException("Bad geographic coordinates!");
        }
    }

    @Override
    public String toString() {
        return latitude + ", " + longitude + " https://maps.google.com/maps?q=" + latitude + "," + longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
