package io.banditoz.mchelper.config;

import com.google.common.collect.Sets;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandPermissions;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.entities.Guild;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Singleton, caching provider for guild-level configurations.
 */
public class ConfigurationProvider {
    private static final Set<Config> allConfigs = new LinkedHashSet<>(List.of(Config.values()));
    private static final Logger log = LoggerFactory.getLogger(ConfigurationProvider.class);
    private boolean notifiedDatabaseDown = false;
    private final GuildConfigDao dao;
    private final Cache<GuildCacheKey, String> cache = Cache2kBuilder.of(GuildCacheKey.class, String.class)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .suppressExceptions(false)
            .permitNullValues(true)
            .build();

    public ConfigurationProvider(MCHelper mcHelper) {
        if (mcHelper.getDatabase() == null && !notifiedDatabaseDown) {
            log.warn("The database is down. All configurations will be default!");
            notifiedDatabaseDown = true;
            this.dao = null;
        }
        else {
            this.dao = new GuildConfigDaoImpl(mcHelper.getDatabase());
        }
    }

    /**
     * Gets a config for this guild, loading it into the cache if it doesn't exist via
     * {@link Cache#computeIfAbsent(Object, Callable)}
     *
     * @return The config value, either what's set in the database, or default if it doesn't exist.
     */
    public String getValue(Config config, long guildId) {
        // guildId == 0 covers DMs
        if (guildId == 0 || dao == null) {
            return config.getDefaultValue();
        }
        else {
            String value = cache.computeIfAbsent(new GuildCacheKey(config, guildId), () -> dao.getConfigValueForGuild(config, guildId));
            if (value == null || value.equals("null")) { // 2nd condition just in case
                return config.getDefaultValue();
            }
            else {
                return value;
            }
        }
    }

    /**
     * Gets a config for this guild, loading it into the cache if it doesn't exist via
     * {@link Cache#computeIfAbsent(Object, Callable)}
     *
     * @return The config value, either what's set in the database, or default if it doesn't exist.
     */
    public String getValue(Config config, Guild guild) {
        return getValue(config, guild == null ? 0 : guild.getIdLong());
    }

    public String getValueOrDefault(Config config, String defaultValue, long guildId) {
        String value = getValue(config, guildId);
        if (Objects.equals(value, config.getDefaultValue())) {
            return defaultValue;
        }
        else {
            return value;
        }
    }

    public void writeValue(Config key, String value, long guildId, long userId) throws SQLException {
        if (key.isBotOwnerLocked() && !CommandPermissions.isBotOwner(userId)) {
            throw new IllegalStateException("You are not a bot owner.");
        }
        dao.writeValue(key, value, guildId, userId);
        cache.put(new GuildCacheKey(key, guildId), value);
    }

    public Map<Long, String> getAllGuildsWith(Config config) {
        // TODO read from cache
        try {
            Map<Long, String> allGuildsWith = dao.getAllGuildsWith(config);
            // load into cache
            allGuildsWith.forEach((k, v) -> cache.put(new GuildCacheKey(config, k), v));
            return allGuildsWith;
        } catch (SQLException ex) {
            log.error("Could not get all guilds with config value " + config + ", returning empty Map...", ex);
            return Collections.emptyMap();
        }
    }

    /**
     * Returns all configs for a {@link Guild} in order sorted by the {@link Config} enum.
     * If a config value doesn't exist for the guild in the DB, it will add its default in the {@link SortedMap}.
     *
     * @param g The {@link Guild} to return all configurations for.
     * @return A map containing all {@link Config Configs.}
     */
    public SortedMap<Config, String> getAllConfigs(Guild g) throws SQLException {
        // TODO read from cache
        SortedMap<Config, String> configs = dao.getAllConfigs(g.getIdLong());
        for (Config configNotPresent : Sets.difference(allConfigs, configs.keySet())) {
            configs.put(configNotPresent, configNotPresent.getDefaultValue());
        }
        configs.forEach((k, v) -> cache.put(new GuildCacheKey(k, g.getIdLong()), v));
        return configs;
    }

    private record GuildCacheKey(Config config, long guildId) {
    }
}
