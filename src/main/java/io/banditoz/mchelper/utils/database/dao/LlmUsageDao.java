package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.LLMUsageLog;

import java.sql.SQLException;

public interface LlmUsageDao {
    void log(LLMUsageLog log) throws SQLException;
}
