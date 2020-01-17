package io.banditoz.mchelper.utils.weather;

import ch.rasc.darksky.model.DsIcon;

public class IconGenerator {
    public static String generateWeatherIcon(DsIcon icon) {
        switch (icon) {
            case CLEAR_DAY:
                return "☼";
            case CLEAR_NIGHT:
                return "\uD83C\uDF19";
            case RAIN:
                return "\uD83C\uDF27";
            case SNOW:
                return "\uD83C\uDF28";
            case SLEET:
                return "\uD83C\uDF27\uD83C\uDF28";
            case WIND:
                return "\uD83C\uDF2C";
            case FOG:
                return "\uD83C\uDF01";
            case CLOUDY:
                return "☁";
            case PARTLY_CLOUDY_DAY:
                return "⛅";
            case PARTLY_CLOUDY_NIGHT:
                return "\uD83C\uDF19☁";
            case UNKNOWN:
                return "?";
        }
        return "?";
    }
}
