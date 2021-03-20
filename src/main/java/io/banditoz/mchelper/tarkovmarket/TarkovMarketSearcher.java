package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.Request;

import java.io.IOException;
import java.util.List;

public class TarkovMarketSearcher {
    private final MCHelper MCHELPER;

    public TarkovMarketSearcher(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    public List<TarkovMarketResult> getMarketResultsBySearch(String search) throws IOException, HttpResponseException {
        ObjectMapper om = MCHELPER.getObjectMapper();
        Request request = new Request.Builder()
                .url("https://tarkov-market.com/api/v1/item?q=" + search)
                .header("x-api-key", MCHELPER.getSettings().getTarkovMarketApiKey())
                .build();
        String json = MCHELPER.performHttpRequest(request);
        return om.readValue(json, om.getTypeFactory().constructCollectionType(List.class, TarkovMarketResult.class));
    }
}
