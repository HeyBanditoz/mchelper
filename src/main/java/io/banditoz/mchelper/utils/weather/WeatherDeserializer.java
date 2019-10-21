package io.banditoz.mchelper.utils.weather;

import ch.rasc.darksky.json.JsonConverter;
import ch.rasc.darksky.model.DsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.MCHelper;

import java.io.IOException;

/**
 * Class with static ObjectMapper to prevent allocating a new one everytime we want the weather.
 */
public class WeatherDeserializer implements JsonConverter {
    private static ObjectMapper om = MCHelper.getObjectMapper();

    @Override
    public DsResponse deserialize(String json) throws IOException {
        return om.readValue(json, DsResponse.class);
    }

    public GeocoderResult[] deserializeReverseGeocoder(String json) throws IOException {
        return om.readValue(json, GeocoderResult[].class);
    }
}
