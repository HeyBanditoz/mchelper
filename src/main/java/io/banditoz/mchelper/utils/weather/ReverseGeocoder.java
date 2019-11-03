package io.banditoz.mchelper.utils.weather;

import java.io.IOException;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.Request;

public class ReverseGeocoder {
    private GeocoderResult[] result;
    private WeatherDeserializer ws;

    public ReverseGeocoder(WeatherDeserializer ws) {
        this.ws = ws;
    }

    public GeoCoordinates reverse(String location) throws IOException, HttpResponseException {
        Request request = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/search/" + location + "?format=json&limit=1")
                .build();
        String responseString = MCHelper.performHttpRequest(request);
        if (responseString.equals("[]")) {
            throw new IllegalArgumentException("Could not find location \"" + location + "\"");
        }
        result = ws.deserializeReverseGeocoder(responseString);
        return new GeoCoordinates(result[0].getLat(), result[0].getLon());
    }

    public String getDisplayName() {
        return this.result[0].getDisplayName();
    }
}

