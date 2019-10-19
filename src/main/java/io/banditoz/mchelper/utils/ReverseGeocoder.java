package io.banditoz.mchelper.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.HttpResponseException;

public class ReverseGeocoder {
    private OkHttpClient c;
    private static ObjectMapper om;
    private GeocoderResult[] result;

    public ReverseGeocoder(OkHttpClient c) {
        this.c = c;
        om = new ObjectMapper();
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
        result = om.readValue(responseString, GeocoderResult[].class);
        return new GeoCoordinates(Double.parseDouble(result[0].getLat()), Double.parseDouble(result[0].getLon()));
    }

    public String getDisplayName() {
        return this.result[0].getDisplayName();
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class GeocoderResult {
    @JsonProperty("place_id")
    private Integer placeId;
    @JsonProperty("licence")
    private String licence;
    @JsonProperty("osm_type")
    private String osmType;
    @JsonProperty("osm_id")
    private Integer osmId;
    @JsonProperty("boundingbox")
    private List<String> boundingbox = null;
    @JsonProperty("lat")
    private String lat;
    @JsonProperty("lon")
    private String lon;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("class")
    private String _class;
    @JsonProperty("type")
    private String type;
    @JsonProperty("importance")
    private Double importance;
    @JsonProperty("icon")
    private String icon;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("lat")
    public String getLat() {
        return lat;
    }

    @JsonProperty("lon")
    public String getLon() {
        return lon;
    }

    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

}
