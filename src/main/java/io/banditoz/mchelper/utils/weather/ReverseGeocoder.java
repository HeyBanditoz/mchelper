package io.banditoz.mchelper.utils.weather;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.HttpResponseException;

public class ReverseGeocoder {
    private OkHttpClient c;
    private GeocoderResult[] result;
    private WeatherDeserializer ws;

    public ReverseGeocoder(OkHttpClient c, WeatherDeserializer ws) {
        this.c = c;
        this.ws = ws;
    }

    public GeoCoordinates reverse(String location) throws IOException {
        Request request = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/search/" + location + "?format=json&limit=1")
                .build();
        Response response = c.newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code(), "Response was not successful! Status code: " + response.code());
        }
        String responseString = response.body().string();
        if (responseString.equals("[]")) {
            throw new IllegalArgumentException("Could not find location \"" + location + "\"");
        }
        result = ws.deserializeReverseGeocoder(responseString);
        return new GeoCoordinates(Double.parseDouble(result[0].getLat()), Double.parseDouble(result[0].getLon()));
    }

    public String getDisplayName() {
        return this.result[0].getDisplayName();
    }
}

