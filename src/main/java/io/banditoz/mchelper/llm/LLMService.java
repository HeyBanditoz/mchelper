package io.banditoz.mchelper.llm;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.UserEvent;
import io.banditoz.mchelper.http.AnthropicClient;
import io.banditoz.mchelper.llm.anthropic.AnthropicRequest;
import io.banditoz.mchelper.llm.anthropic.AnthropicResponse;
import io.banditoz.mchelper.llm.anthropic.Usage;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.LLMUsageLog;
import io.banditoz.mchelper.utils.database.dao.LlmUsageDao;
import io.banditoz.mchelper.utils.database.dao.LlmUsageDaoImpl;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

public class LLMService {
    private final AnthropicClient anthropicClient;
    private final LongCounter tokenCounter;
    private final LlmUsageDao llmUsageDao;

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    public LLMService(MCHelper mcHelper) {
        this.anthropicClient = mcHelper.getHttp().getAnthropicClient();
        this.tokenCounter = mcHelper.getOTel().meter()
                .meterBuilder("llm_metrics")
                .build()
                .counterBuilder("llm_tokens")
                .setDescription("Counter tracking tokens from an LLM, including model and consumed tokens.")
                .build();

        Database database = mcHelper.getDatabase();
        if (database == null) {
            log.warn("The database is not configured. LLM usage statistics will only be visible to the counters.");
            this.llmUsageDao = new LlmUsageDaoImpl.LlmUsageDaoNoopImpl();
        }
        else {
            this.llmUsageDao = new LlmUsageDaoImpl(database);
        }
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
            llmUsageDao.log(llmUsageLog);
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
