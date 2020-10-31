package io.banditoz.mchelper.urbandictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.dictionary.DictionarySearcher;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.Request;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UDSearcher {
    private final MCHelper MCHELPER;
    private static final Cache<String, UDResult> CACHE = new Cache2kBuilder<String, UDResult>() {}.expireAfterWrite(1, TimeUnit.DAYS).suppressExceptions(false).build();
    private final Logger LOGGER = LoggerFactory.getLogger(DictionarySearcher.class);

    public UDSearcher(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    public UDResult search(String word) throws IOException, HttpResponseException {
        // first, search the cache
        if (CACHE.get(word) != null) {
            LOGGER.debug("Cache hit for " + word);
            return CACHE.get(word);
        }
        else {
            Request request = new Request.Builder()
                    .url("https://api.urbandictionary.com/v0/define?term=" + word)
                    .build();
            String json = MCHELPER.performHttpRequest(request);
            LOGGER.debug(json);
            UDResult definition = MCHELPER.getObjectMapper().readValue(json, UDResult.class);
            definition.getResults().sort(UDDefinition::compareTo); // highest votes first
            CACHE.put(word, definition);
            return definition;
        }
    }
}
