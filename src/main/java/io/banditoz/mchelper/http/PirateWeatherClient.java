package io.banditoz.mchelper.http;

import feign.Param;
import feign.RequestLine;
import io.banditoz.mchelper.weather.darksky.DSWeather;

public interface PirateWeatherClient extends DarkSkyClient {
    @Override
    @RequestLine("GET /forecast/APIKEY/{lat},{lng}?units=us&exclude=alerts")
    DSWeather getForecast(@Param("lat") String lat, @Param("lng") String lng);

    @Override
    @RequestLine("GET /forecast/APIKEY/{lat},{lng}?units=us&exclude=minutely,hourly,alerts&tz=precise")
    DSWeather getCurrentlyDaily(@Param("lat") String lat, @Param("lng") String lng);
}
