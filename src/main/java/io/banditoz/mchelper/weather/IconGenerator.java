package io.banditoz.mchelper.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconGenerator {
    private static final Logger log = LoggerFactory.getLogger(IconGenerator.class);

    public static String generateWeatherIcon(String icon) {
        return switch (icon) {
            case "clear-day" -> "\uD83C\uDF1E";
            case "clear-night" -> "\uD83C\uDF15";
            case "rain" -> "\uD83C\uDF27";
            case "snow" -> "\uD83C\uDF28";
            case "sleet" -> "\uD83C\uDF27\uD83C\uDF28";
            case "wind" -> "\uD83C\uDF2C";
            case "fog" -> "\uD83C\uDF2B️";
            case "cloudy" -> "☁";
            case "partly-cloudy-day" -> "⛅";
            case "partly-coudy-night" -> "\uD83C\uDF12";
            default -> {
                log.warn("icon={} is unknown", icon);
                yield "❓";
            }
        };
    }
}
