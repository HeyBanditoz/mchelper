package io.banditoz.mchelper.llm.anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.banditoz.mchelper.llm.Message;

import java.util.List;

/**
 * A request to an LLM, using an Anthropic-compatible API.
 *
 * @param model    The LLM model to use.
 * @param messages List of messages to send.
 * @param maxTokens Maximuum number of tokens to respond with.
 * @param system The <a href="https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/system-prompts">system prompt</a>
 *               to use.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnthropicRequest(String model,
                               @JsonProperty("max_tokens") int maxTokens,
                               List<Message> messages,
                               String system,
                               double temperature) {

    @Override
    public String toString() {
        return "LLMRequest{" +
                "model='" + model + '\'' +
                ", maxTokens=" + maxTokens +
                ", messagesCount=" + (messages == null ? 0 : messages.size()) +
                '}';
    }


    public static final class PromptBuilder {
        private String model;
        private int maxTokens;
        private String initialMessage;
        private String system;
        private double temperature = 1.0; // Anthropic's current default

        public PromptBuilder() {
        }

        public PromptBuilder setModel(String val) {
            model = val;
            return this;
        }

        public PromptBuilder setMaxTokens(int val) {
            maxTokens = val;
            return this;
        }

        public PromptBuilder setInitialMessage(String val) {
            initialMessage = val;
            return this;
        }

        public PromptBuilder setSystem(String val) {
            system = val;
            return this;
        }

        public PromptBuilder setTemperature(double val) {
            temperature = val;
            return this;
        }

        public AnthropicRequest build() {
            Message m = new Message("user", initialMessage);
            return new AnthropicRequest(model, maxTokens, List.of(m), system, temperature);
        }
    }
}
