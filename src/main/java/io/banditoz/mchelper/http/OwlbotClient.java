package io.banditoz.mchelper.http;

import feign.Param;
import feign.RequestLine;
import io.banditoz.mchelper.dictionary.DictionaryResult;

public interface OwlbotClient {
    @RequestLine("GET /v3/dictionary/{word}")
    DictionaryResult getDefinition(@Param("word") String word);
}
