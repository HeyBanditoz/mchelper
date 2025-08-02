package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;

import io.banditoz.mchelper.database.LLMUsageLog;

public interface LlmUsageDao {
    void log(LLMUsageLog log) throws SQLException;
}
