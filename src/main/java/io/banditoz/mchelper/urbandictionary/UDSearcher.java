package io.banditoz.mchelper.urbandictionary;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.banditoz.mchelper.http.UrbanDictionaryClient;
import io.banditoz.mchelper.utils.HttpResponseException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

@Singleton
public class UDSearcher {
    private final UrbanDictionaryClient client;
    private static final Cache<String, UDResult> CACHE = new Cache2kBuilder<String, UDResult>() {}.expireAfterWrite(1, TimeUnit.DAYS).suppressExceptions(false).build();

    @Inject
    public UDSearcher(UrbanDictionaryClient client) {
        this.client = client;
    }

    public UDResult search(String word) throws IOException, HttpResponseException {
        UDResult definition = CACHE.computeIfAbsent(word, () -> client.getResultsByDefinition(word));
        definition.getResults().sort(UDDefinition::compareTo); // highest votes first
        return definition;
    }
}
