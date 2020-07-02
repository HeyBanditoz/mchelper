package io.banditoz.mchelper.utils.weather.es;

import com.fasterxml.jackson.databind.JsonNode;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

public class Weather {
    private final MCHelper MCHELPER;

    public Weather(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    public String getLatestFormattedWeather() throws IOException, HttpResponseException {
        Request request = new Request.Builder()
                .url(MCHELPER.getSettings().getEsUrl() + "weather/_search")
                .post(RequestBody.create(MediaType.get("application/json"), "{\"query\":{\"match_all\":{}},\"size\":1,\"sort\":[{\"@timestamp\":{\"order\":\"desc\"}}]}"))
                .build();
        Response response = MCHELPER.performHttpRequestGetResponse(request);
        JsonNode jn = MCHELPER.getObjectMapper().readTree(response.body().string());

        MathContext m = new MathContext(4);
        BigDecimal currentFahrenheit = new BigDecimal(returnValue(jn, "fahrenheit"));
        BigDecimal currentCelsius = new BigDecimal(returnValue(jn, "celsius"));
        BigDecimal currentHumidity = new BigDecimal(returnValue(jn, "humidity"));
        BigDecimal currenthPa = new BigDecimal(returnValue(jn, "hpa"));
        response.close();

        return currentFahrenheit + " °F, " + currentCelsius + " °C, " + currentHumidity + " %H, " + currenthPa + " hPa (" + currenthPa.divide(new BigDecimal("1013.25"), m) + " atm, " + currenthPa.divide(new BigDecimal("68.9475729318"), m) + " psi)";
    }

    public double getFahrenheit() throws IOException, HttpResponseException {
        Request request = new Request.Builder()
                .url(MCHELPER.getSettings().getEsUrl() + "weather/_search")
                .post(RequestBody.create(MediaType.get("application/json"), "{\"query\":{\"match_all\":{}},\"size\":1,\"sort\":[{\"@timestamp\":{\"order\":\"desc\"}}]}"))
                .build();
        Response response = MCHELPER.performHttpRequestGetResponse(request);
        JsonNode jn = MCHELPER.getObjectMapper().readTree(response.body().string());
        response.close();

        return Double.parseDouble(returnValue(jn, "fahrenheit"));
    }

    private String returnValue(JsonNode jn, String value) {
        return jn.get("hits").get("hits").get(0).get("_source").get(value).toString();
    }
}