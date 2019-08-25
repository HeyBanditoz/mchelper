package io.banditoz.mchelper.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CryptoTicker implements Ticker {
    private final String API;
    private String response;

    public CryptoTicker(String currencies) throws IOException, URISyntaxException {
        Settings settings = SettingsManager.getInstance().getSettings();
        API = settings.getCoinMarketCapAPIKey();
        currencies = currencies.replace("\\s+", "");

        String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("symbol", currencies));

        makeAPICall(uri, parameters);

        JsonParser parser = new JsonParser();

        JsonElement jsonElement = parser.parse(response);

        JsonObject o = jsonElement.getAsJsonObject();

    }

    @Override
    public double getPrice() {
        return 0;
    }

    @Override
    public double get24h() {
        return 0;
    }

    @Override
    public void makeAPICall(String uri, List<NameValuePair> parameters) throws URISyntaxException, IOException {
        String response_content = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", API);

        CloseableHttpResponse response = client.execute(request);

        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        this.response = response_content;
        System.out.println(this.response);
    }
}
