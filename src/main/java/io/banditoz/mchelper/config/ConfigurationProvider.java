package io.banditoz.mchelper.config;

import com.google.common.collect.Sets;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigurationProvider {
    private static final Set<Config> allConfigs = new LinkedHashSet<>(List.of(Config.values()));
    private static final Logger log = LoggerFactory.getLogger(ConfigurationProvider.class);
    private static final AtomicBoolean notifiedDatabaseDown = new AtomicBoolean(false);

    private final GuildConfigDao dao;
    private final Settings settings;

    public ConfigurationProvider(MCHelper mcHelper) {
        if (mcHelper.getDatabase() == null && !notifiedDatabaseDown.get()) {
            log.warn("The database is down. All configurations will be default!");
            notifiedDatabaseDown.set(true);
            this.dao = null;
        }
        else {
            this.dao = new GuildConfigDaoImpl(mcHelper.getDatabase());
        }
        this.settings = mcHelper.getSettings();
    }

    public String getValue(Config config, long guildId) {
        if (guildId == 0 || dao == null) {
            return config.getDefaultValue();
        }
        else {
            String value = dao.getConfigValueForGuild(config, guildId);
            if (value == null || value.equals("null")) { // 2nd condition just in case
                return config.getDefaultValue();
            }
            else {
                return value;
            }
        }
    }

    public String getValueOrDefault(Config config, String defaultValue, long guildId) {
        String value = getValue(config, guildId);
        if (value.equals(config.getDefaultValue())) {
            return defaultValue;
        }
        else {
            return value;
        }
    }

    public void writeValue(Config key, String value, long guildId, long userId) throws SQLException {
        if (key.isBotOwnerLocked() && !settings.getBotOwners().contains(String.valueOf(userId))) {
            throw new IllegalStateException("You are not a bot owner.");
        }
        dao.writeValue(key, value, guildId, userId);
    }

    public Map<Long, String> getAllGuildsWith(Config config) {
        try {
            return dao.getAllGuildsWith(config);
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
        SortedMap<Config, String> configs = dao.getAllConfigs(g.getIdLong());
        for (Config configNotPresent : Sets.difference(allConfigs, configs.keySet())) {
            configs.put(configNotPresent, configNotPresent.getDefaultValue());
        }
        return configs;
    }
}
