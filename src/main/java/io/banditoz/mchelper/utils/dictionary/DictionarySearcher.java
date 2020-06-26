package io.banditoz.mchelper.utils.dictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DictionarySearcher {
    private final MCHelper MCHELPER;
    private static final DictionaryCache CACHE = new DictionaryCache();
    private final Logger LOGGER = LoggerFactory.getLogger(DictionarySearcher.class);

    public DictionarySearcher(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    public DictionaryResult search(String word) throws IOException, HttpResponseException {
        // first, search the cache
        if (CACHE.getDefinition(word) != null) {
            LOGGER.debug("Cache hit for " + word);
            return CACHE.getDefinition(word);
        }
        else {
            Request request = new Request.Builder()
                    .url("https://owlbot.info/api/v3/dictionary/" + word)
                    .addHeader("Authorization", "Token " + MCHELPER.getSettings().getOwlBotToken())
                    .build();
            String json = MCHELPER.performHttpRequest(request);
            LOGGER.debug(json);
            DictionaryResult definition = MCHELPER.getObjectMapper().readValue(json, DictionaryResult.class);
            CACHE.putDefinition(definition);
            return definition;
        }
    }
}
