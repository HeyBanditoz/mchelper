package io.banditoz.mchelper.weather.darksky;

import java.util.Locale;

public interface Precipitationable {
    String precipType();

    /**
     * @return Capitalized precipType, i.e. <i>Snow</i> or <i>Rain</i>. If the API is none or null, returns empty String
     * instead.
     */
    default String getFormattedPrecipType() {
        if (!"none".equals(precipType())) {
            return ' ' + precipType().substring(0, 1).toUpperCase(Locale.ROOT) + precipType().substring(1);
        }
        else {
            return "";
        }
    }
}
