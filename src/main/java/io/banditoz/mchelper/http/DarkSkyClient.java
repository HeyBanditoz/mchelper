package io.banditoz.mchelper.http;

import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.geocoder.Location;

public interface DarkSkyClient {
    DSWeather getForecast(String lat, String lng);
    DSWeather getCurrentlyDaily(String lat, String lng);

    default DSWeather getForecast(Location loc) {
        return getForecast(loc.lat(), loc.lon());
    }

    default DSWeather getCurrentlyDaily(Location loc) {
        return getCurrentlyDaily(loc.lat(), loc.lon());
    }
}
