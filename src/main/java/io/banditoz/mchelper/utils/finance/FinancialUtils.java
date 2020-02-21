package io.banditoz.mchelper.utils.finance;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.SettingsManager;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.io.IOException;

public class FinancialUtils {
    private final static String API_KEY = SettingsManager.getInstance().getSettings().getAlphaVantageKey();

    public static RealtimeCurrencyExchangeRate getCurrencyExchangeRate(String from, String to) throws IOException, HttpResponseException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.alphavantage.co")
                .addPathSegment("query")
                .addQueryParameter("function", "CURRENCY_EXCHANGE_RATE")
                .addQueryParameter("from_currency", from)
                .addQueryParameter("to_currency", to)
                .addQueryParameter("apikey", API_KEY)
                .build();
        System.out.println(url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        return MCHelper.getObjectMapper().readValue(MCHelper.performHttpRequest(request), Currency.class).getRealtimeCurrencyExchangeRate();
    }
}
