package io.banditoz.mchelper.database.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.database.LLMUsageLog;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresDatabase
public class LlmUsageDaoImpl extends Dao implements LlmUsageDao {
    @Inject
    public LlmUsageDaoImpl(Database database) {
        super(database);
    }

    @Override
    public void log(LLMUsageLog log) throws SQLException {
        try (Connection c = database.getConnection()) {
            Query.of("""
                     INSERT INTO llm_usage
                     (guild_id, user_id, source, llm_provider, model, input_tokens_used, output_tokens_used, time_took_ms)
                     VALUES (:g, :u, :s, :l, :m, :i, :o, :t)""")
                    .on(
                            Param.value("g", Optional.ofNullable(log.guildId())),
                            Param.value("u", log.userId()),
                            Param.value("s", log.source()),
                            Param.value("l", log.llmProvider()),
                            Param.value("m", log.model()),
                            Param.value("i", log.inputTokensUsed()),
                            Param.value("o", log.outputTokensUsed()),
                            Param.value("t", log.timeTookMs())
                    ).execute(c);
        }
    }

    public static class LlmUsageDaoNoopImpl implements LlmUsageDao {
        @Override
        public void log(LLMUsageLog log) throws SQLException {
            // no-op
        }
    }
}
