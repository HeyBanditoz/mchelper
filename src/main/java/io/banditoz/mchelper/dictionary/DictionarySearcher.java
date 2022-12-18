package io.banditoz.mchelper.dictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.http.OwlbotClient;
import io.banditoz.mchelper.utils.HttpResponseException;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DictionarySearcher {
    private final OwlbotClient client;
    private static final Cache<String, DictionaryResult> CACHE = new Cache2kBuilder<String, DictionaryResult>() {}.eternal(true).suppressExceptions(false).build();
    private final Logger LOGGER = LoggerFactory.getLogger(DictionarySearcher.class);

    public DictionarySearcher(MCHelper mcHelper) {
        this.client = mcHelper.getHttp().getOwlbotCLient();
    }

    public DictionaryResult search(String word) throws IOException, HttpResponseException {
        // first, search the cache
        if (CACHE.get(word) != null) {
            LOGGER.debug("Cache hit for " + word);
            return CACHE.get(word);
        }
        else {
            return client.getDefinition(word);
        }
    }
}
