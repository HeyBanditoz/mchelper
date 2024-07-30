package io.banditoz.mchelper.llm.anthropic;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnthropicResponse(String id, String model, String role, String stopReason, String stopSequence,
                                String type, Usage usage, List<AnthropicContent> content) {
}
