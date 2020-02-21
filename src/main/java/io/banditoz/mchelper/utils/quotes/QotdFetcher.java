package io.banditoz.mchelper.utils.quotes;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.Request;

import java.io.IOException;

public class QotdFetcher {
    /**
     * Gets the current quote of the day.
     * @return The quote item
     */
    public static QuoteItem getQotd() throws IOException, HttpResponseException {
        Request request = new Request.Builder()
                .url("https://quotes.rest/qod.json")
                .build();
        return MCHelper.getObjectMapper().readValue(MCHelper.performHttpRequest(request), Quote.class).getContents().getQuotes().get(0);
    }
}
