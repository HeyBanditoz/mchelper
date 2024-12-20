package io.banditoz.mchelper.llm.anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnthropicResponse(String id, String model, String role, String stopReason, String stopSequence,
                                String type, Usage usage, List<AnthropicContent> content) {
}
