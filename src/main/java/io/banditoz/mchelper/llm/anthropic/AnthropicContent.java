package io.banditoz.mchelper.llm.anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnthropicContent(String type, String text) {
}
