package io.banditoz.mchelper.utils.weather.es;

import com.fasterxml.jackson.databind.JsonNode;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.SettingsManager;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class EsUtils {
    public static String getLatestFormattedWeather() throws IOException {
        Request request = new Request.Builder()
                .url(SettingsManager.getInstance().getSettings().getEsUrl() + "weather/_search")
                .post(RequestBody.create(MediaType.get("application/json"), "{\"query\":{\"match_all\":{}},\"size\":1,\"sort\":[{\"@timestamp\":{\"order\":\"desc\"}}]}"))
                .build();
        Response response = MCHelper.getOkHttpClient().newCall(request).execute();
        JsonNode jn = MCHelper.getObjectMapper().readTree(response.body().string());
        return returnValue(jn, "fahrenheit") + " °F, " + returnValue(jn, "humidity") + " %H, " + returnValue(jn, "hpa") + " hPa";
    }

    private static String returnValue(JsonNode jn, String value) {
        return jn.get("hits").get("hits").get(0).get("_source").get(value).toString();
    }
}
