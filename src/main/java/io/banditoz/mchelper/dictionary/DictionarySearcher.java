package io.banditoz.mchelper.dictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.http.OwlbotClient;
import io.banditoz.mchelper.utils.HttpResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DictionarySearcher {
    private final OwlbotClient client;
    private static final Map<String, DictionaryResult> CACHE = new HashMap<>();

    public DictionarySearcher(MCHelper mcHelper) {
        this.client = mcHelper.getHttp().getOwlbotCLient();
    }

    public DictionaryResult search(String word) throws IOException, HttpResponseException {
        return CACHE.computeIfAbsent(word, client::getDefinition);
    }
}
