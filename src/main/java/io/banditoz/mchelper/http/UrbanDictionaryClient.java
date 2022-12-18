package io.banditoz.mchelper.http;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.banditoz.mchelper.urbandictionary.UDResult;

@Headers({"Accept: application/json"})
public interface UrbanDictionaryClient {
    @RequestLine("GET /v0/define?term={term}")
    UDResult getResultsByDefinition(@Param("term") String term);
}
