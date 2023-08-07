package io.banditoz.mchelper.weather.geocoder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Location(String osmType, String osmId, double importance, String icon, String lon, String displayName,
                       String type, String jsonMemberClass, int placeId, String lat) {
}
