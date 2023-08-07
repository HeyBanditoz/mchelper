package io.banditoz.mchelper.http;

import io.banditoz.mchelper.weather.darksky.DSWeather;

public interface DarkSkyClient {
    DSWeather getForecast(String lat, String lng);
}
