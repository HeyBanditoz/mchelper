package io.banditoz.mchelper.utils.quotes;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.Request;

import java.io.IOException;

public class QotdFetcher {
    private final MCHelper MCHELPER;

    public QotdFetcher(MCHelper mchelper) {
        this.MCHELPER = mchelper;
    }

    /**
     * Gets the current quote of the day.
     *
     * @return The quote item
     */
    public Quote getQotd() throws IOException, HttpResponseException {
        Request request = new Request.Builder()
                .url("https://favqs.com/api/qotd ")
                .build();
        return MCHELPER.getObjectMapper().readValue(MCHELPER.performHttpRequest(request), QuoteHolder.class).getQuote();
    }
}
