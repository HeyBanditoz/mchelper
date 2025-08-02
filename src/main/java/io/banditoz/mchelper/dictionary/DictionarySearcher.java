package io.banditoz.mchelper.dictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.banditoz.mchelper.http.OwlbotClient;
import io.banditoz.mchelper.utils.HttpResponseException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DictionarySearcher {
    private final OwlbotClient client;
    private static final Map<String, DictionaryResult> CACHE = new HashMap<>();

    @Inject
    public DictionarySearcher(OwlbotClient client) {
        this.client = client;
    }

    public DictionaryResult search(String word) throws IOException, HttpResponseException {
        return CACHE.computeIfAbsent(word, client::getDefinition);
    }
}
