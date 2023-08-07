package io.banditoz.mchelper.weather.darksky;

import java.time.Instant;

public record DataItem(double windGust, int apparentTemperatureMinTime, double temperatureMax, String icon,
                       double precipIntensityMax, int windBearing, int temperatureMaxTime,
                       double apparentTemperatureMin, int sunsetTime, double temperatureLow, String precipType,
                       double humidity, double moonPhase, double windSpeed, int apparentTemperatureLowTime,
                       int sunriseTime, double apparentTemperatureLow, String summary, double precipProbability,
                       int temperatureHighTime, double visibility, double precipIntensity, double cloudCover,
                       double temperatureMin, int apparentTemperatureHighTime, double pressure, double dewPoint,
                       int temperatureMinTime, int uvIndexTime, double apparentTemperatureMax, double temperatureHigh,
                       int temperatureLowTime, double precipAccumulation, double apparentTemperatureHigh, Instant time,
                       int precipIntensityMaxTime, int windGustTime, double uvIndex, int apparentTemperatureMaxTime,
                       double precipIntensityError, double apparentTemperature, double ozone, double temperature) {
}