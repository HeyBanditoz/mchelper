package io.banditoz.mchelper.mtg;

import java.util.Optional;

import feign.FeignException;
import io.banditoz.mchelper.http.ScryfallClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HttpScryfallCardLookup {
    private final ScryfallClient scryfallClient;

    @Inject
    public HttpScryfallCardLookup(ScryfallClient scryfallClient) {
        this.scryfallClient = scryfallClient;
    }

    /**
     * Finds the card best matching a name, per what Scryfall's API thinks.
     *
     * @param search The name as the user typed it.
     * @return The best matching card, or empty if nothing matches.
     * @throws FeignException If Scryfall responds with an error other than not found.
     */
    public Optional<ScryfallCard> findByFuzzyName(String search) {
        try {
            return Optional.of(scryfallClient.getCardByFuzzySearch(search));
        } catch (FeignException.NotFound ex) {
            return Optional.empty();
        }
    }
}
