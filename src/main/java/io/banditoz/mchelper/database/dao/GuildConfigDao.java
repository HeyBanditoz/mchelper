package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;

import io.banditoz.mchelper.config.Config;

public interface GuildConfigDao {
    String getConfigValueForGuild(Config config, long guildId);
    SortedMap<Config, String> getAllConfigs(long guildId) throws SQLException;
    Map<Long, String> getAllGuildsWith(Config config) throws SQLException;
    void writeValue(Config config, String value, long guildId, long userId) throws SQLException;
}
