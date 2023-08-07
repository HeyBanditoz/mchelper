package io.banditoz.mchelper.weather.darksky;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DSWeather(int elevation, List<AlertsItem> alerts, double offset, Currently currently, String timezone,
                        double latitude, Daily daily, Hourly hourly, Minutely minutely, double longitude) {
}