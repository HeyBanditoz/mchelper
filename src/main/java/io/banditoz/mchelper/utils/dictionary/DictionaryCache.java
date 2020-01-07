package io.banditoz.mchelper.utils.dictionary;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

public class DictionaryCache {
    private final Cache<String, DictionaryResult> cache;

    public DictionaryCache() {
        this.cache = new Cache2kBuilder<String, DictionaryResult>() {}
                .eternal(true) // i doubt the english language will change during the bot's uptime
                .suppressExceptions(false)
                .build();
    }

    public DictionaryResult getDefinition(String word) {
        return cache.get(word);
    }

    public void putDefinition(DictionaryResult result) {
        cache.put(result.getWord(), result);
    }
}
