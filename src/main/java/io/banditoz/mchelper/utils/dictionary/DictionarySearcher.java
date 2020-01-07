package io.banditoz.mchelper.utils.dictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.SettingsManager;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DictionarySearcher {
    private static DictionaryCache cache = new DictionaryCache();
    private static Logger logger = LoggerFactory.getLogger(DictionarySearcher.class);

    public static DictionaryResult search(String word) throws IOException, HttpResponseException {
        // first, search the cache
        if (cache.getDefinition(word) != null) {
            logger.debug("Cache hit for " + word);
            return cache.getDefinition(word);
        }
        else {
            Request request = new Request.Builder()
                    .url("https://owlbot.info/api/v3/dictionary/" + word)
                    .addHeader("Authorization", "Token " + SettingsManager.getInstance().getSettings().getOwlBotToken())
                    .build();
            String json = MCHelper.performHttpRequest(request);
            logger.debug(json);
            DictionaryResult definition = MCHelper.getObjectMapper().readValue(json, DictionaryResult.class);
            cache.putDefinition(definition);
            return definition;
        }
    }
}
