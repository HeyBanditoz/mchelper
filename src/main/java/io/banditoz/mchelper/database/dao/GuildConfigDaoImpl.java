package io.banditoz.mchelper.database.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
public class GuildConfigDaoImpl extends Dao implements GuildConfigDao {
    private static final Logger log = LoggerFactory.getLogger(GuildConfigDaoImpl.class);

    @Inject
    public GuildConfigDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getConfigValueForGuild(Config config, long guildId) {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT value FROM guild_config WHERE guild_id = :g AND key = :k")
                    .on(
                            Param.value("g", guildId),
                            Param.value("k", config.name())
                    )
                    .as((rs, conn) -> rs.next() ? rs.getString(1) : null, c);
        } catch (SQLException ex) {
            log.warn("Encountered SQLException fetching config value " + config.name() + " for guild " + guildId + ". Returning default value.", ex);
            return config.getDefaultValue();
        }
    }

    @Override
    public SortedMap<Config, String> getAllConfigs(long guildId) throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT key, value FROM guild_config WHERE guild_id = :g")
                    .on(Param.value("g", guildId))
                    .as((rs, conn) -> {
                        SortedMap<Config, String> map = new TreeMap<>();
                        while (rs.next()) {
                            try {
                                Config config = Config.valueOf(rs.getString(1));
                                String value = rs.getString(2);
                                map.put(config, value);
                            } catch (IllegalArgumentException ex) {
                                log.warn("Exception parsing Config stored in DB to its respective enum. For value "
                                        + rs.getString(1) + ". Bad config name? Skipping.", ex);
                            }
                        }
                        return map;
                    }, c);
        }
    }

    @Override
    public Map<Long, String> getAllGuildsWith(Config config) throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT guild_id, value FROM guild_config WHERE key = :k")
                    .on(Param.value("k", config.name()))
                    .as((rs, conn) -> {
                        Map<Long, String> map = new HashMap<>();
                        while (rs.next()) {
                            map.put(rs.getLong(1), rs.getString(2));
                        }
                        return map;
                    }, c);
        }
    }

    @Override
    public void writeValue(Config config, String value, long guildId, long userId) throws SQLException {
        try (Connection c = database.getConnection()) {
            Query.of("""
                    INSERT INTO guild_config (guild_id, key, value, created_by)
                    VALUES (:g, :k, :v, :c)
                    ON CONFLICT (guild_id, key) DO UPDATE SET value      = excluded.value,
                                                              created_on = NOW(),
                                                              created_by = excluded.created_by
                    """)
                    .on(
                            Param.value("g", guildId),
                            Param.value("k", config.name()),
                            Param.value("v", value == null ? config.getDefaultValue() : value),
                            Param.value("c", userId)
                    ).executeInsert(c);
        }
    }
}
