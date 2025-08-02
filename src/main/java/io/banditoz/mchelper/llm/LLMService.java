package io.banditoz.mchelper.llm;

import javax.annotation.Nullable;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.avaje.inject.RequiresProperty;
import io.banditoz.mchelper.UserEvent;
import io.banditoz.mchelper.database.LLMUsageLog;
import io.banditoz.mchelper.database.dao.LlmUsageDao;
import io.banditoz.mchelper.http.AnthropicClient;
import io.banditoz.mchelper.llm.anthropic.AnthropicRequest;
import io.banditoz.mchelper.llm.anthropic.AnthropicResponse;
import io.banditoz.mchelper.llm.anthropic.Usage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresProperty(value = "mchelper.anthropic.token")
public class LLMService {
    private final AnthropicClient anthropicClient;
    private final LongCounter tokenCounter;
    private final LlmUsageDao llmUsageDao;

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    @Inject
    public LLMService(AnthropicClient anthropicClient,
                      MeterProvider meterProvider,
                      @Nullable LlmUsageDao llmUsageDao) {
        this.anthropicClient = anthropicClient;
        this.tokenCounter = meterProvider
                .meterBuilder("llm_metrics")
                .build()
                .counterBuilder("llm_tokens")
                .setDescription("Counter tracking tokens from an LLM, including model and consumed tokens.")
                .build();
        this.llmUsageDao = llmUsageDao;
    }

    public String getSingleResponse(AnthropicRequest request, UserEvent userEvent) {
        long before = System.currentTimeMillis();
        AnthropicResponse anthropicResponse = anthropicClient.generateMessage(request);
        Usage usage = anthropicResponse.usage();

        // log it to logback, and insert a row into the llm_usage table
        try {
            LLMUsageLog llmUsageLog = new LLMUsageLog.Builder()
                    .setGuildId(userEvent.getGuild() == null ? null : userEvent.getGuild().getIdLong())
                    .setUserId(userEvent.getUserId())
                    .setSource(userEvent.commandName())
                    .setLlmProvider("Anthropic")
                    .setModel(request.model())
                    .setInputTokensUsed(usage.inputTokens())
                    .setOutputTokensUsed(usage.outputTokens())
                    .setTimeTookMs((int) (System.currentTimeMillis() - before))
                    .build();
            log.info("LLM request finished. details={}", llmUsageLog);
            if (llmUsageDao != null) {
                llmUsageDao.log(llmUsageLog);
            }
        } catch (Exception e) {
            log.error("Error while recording a LLM stat!", e);
        }

        // and add to the counters
        tokenCounter.add(usage.inputTokens(), Attributes.of(
                stringKey("model"), request.model(),
                stringKey("kind"), "input"
        ));
        tokenCounter.add(usage.outputTokens(), Attributes.of(
                stringKey("model"), request.model(),
                stringKey("kind"), "output"
        ));


        if (anthropicResponse.content() == null) {
            throw new IllegalArgumentException("Content was null on AnthropicResponse");
        }
        if (anthropicResponse.content().isEmpty()) {
            throw new IllegalArgumentException("Content was empty on AnthropicResponse");
        }
        return anthropicResponse.content().get(0).text();
    }
}
