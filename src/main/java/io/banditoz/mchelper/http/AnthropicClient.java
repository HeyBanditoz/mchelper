package io.banditoz.mchelper.http;

import feign.Headers;
import feign.RequestLine;
import io.banditoz.mchelper.llm.anthropic.AnthropicRequest;
import io.banditoz.mchelper.llm.anthropic.AnthropicResponse;

@Headers({"Accept: application/json", "Content-Type: application/json", "anthropic-version: 2023-06-01"})
public interface AnthropicClient {
    @RequestLine("POST /v1/messages")
    AnthropicResponse generateMessage(AnthropicRequest request);
}
