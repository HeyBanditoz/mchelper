package io.banditoz.mchelper.weather.darksky;

public record Currently(String summary, double precipProbability, double visibility, double windGust,
                        double precipIntensity, double precipIntensityError, String icon, double cloudCover,
                        int windBearing, double apparentTemperature, double pressure, double dewPoint, double ozone,
                        int nearestStormBearing, int nearestStormDistance, String precipType, double temperature,
                        double humidity, int time, double windSpeed, double uvIndex)
        implements Precipitationable {
}