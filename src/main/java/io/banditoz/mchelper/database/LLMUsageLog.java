package io.banditoz.mchelper.database;

public record LLMUsageLog(Long guildId, long userId, String source,
                          String llmProvider, String model,
                          int inputTokensUsed, int outputTokensUsed,
                          long timeTookMs) {

    public static final class Builder {
        private Long guildId;
        private long userId;
        private String source;
        private String llmProvider;
        private String model;
        private int inputTokensUsed;
        private int outputTokensUsed;
        private int timeTookMs;

        public Builder setGuildId(Long val) {
            guildId = val;
            return this;
        }

        public Builder setUserId(long val) {
            userId = val;
            return this;
        }

        public Builder setSource(String val) {
            source = val;
            return this;
        }

        public Builder setLlmProvider(String val) {
            llmProvider = val;
            return this;
        }

        public Builder setModel(String val) {
            model = val;
            return this;
        }

        public Builder setInputTokensUsed(int val) {
            inputTokensUsed = val;
            return this;
        }

        public Builder setOutputTokensUsed(int val) {
            outputTokensUsed = val;
            return this;
        }

        public Builder setTimeTookMs(int val) {
            timeTookMs = val;
            return this;
        }

        public LLMUsageLog build() {
            return new LLMUsageLog(guildId, userId, source, llmProvider, model, inputTokensUsed, outputTokensUsed, timeTookMs);
        }
    }
}
