package io.banditoz.mchelper.urbandictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.http.UrbanDictionaryClient;
import io.banditoz.mchelper.utils.HttpResponseException;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UDSearcher {
    private final UrbanDictionaryClient client;
    private static final Cache<String, UDResult> CACHE = new Cache2kBuilder<String, UDResult>() {}.expireAfterWrite(1, TimeUnit.DAYS).suppressExceptions(false).build();

    public UDSearcher(MCHelper mcHelper) {
        this.client = mcHelper.getHttp().getUrbanDictionaryClient();
    }

    public UDResult search(String word) throws IOException, HttpResponseException {
        UDResult definition = CACHE.computeIfAbsent(word, () -> client.getResultsByDefinition(word));
        definition.getResults().sort(UDDefinition::compareTo); // highest votes first
        return definition;
    }
}
