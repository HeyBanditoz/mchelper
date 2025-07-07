package io.banditoz.mchelper.http;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.banditoz.mchelper.mtg.ScryfallCard;

@Headers({"Accept: application/json", "Content-Type: application/json"})
public interface ScryfallClient {
    @RequestLine("GET /cards/named?fuzzy={search}")
    ScryfallCard getCardByFuzzySearch(@Param("search") String search);
}
