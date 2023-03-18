package io.banditoz.mchelper.config;

import com.google.common.collect.Sets;
import io.banditoz.mchelper.utils.database.Database;
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

    public ConfigurationProvider(Database database) {
        if (database == null && !notifiedDatabaseDown.get()) {
            log.warn("The database is down. All configurations will be default!");
            notifiedDatabaseDown.set(true);
            this.dao = null;
        }
        else {
            this.dao = new GuildConfigDaoImpl(database);
        }
    }

    public String getValue(Config c, long g) {
        if (g == 0 || dao == null) {
            return c.getDefaultValue();
        }
        else {
            String value = dao.getConfigValueForGuild(c, g);
            if (value == null || value.equals("null")) { // 2nd condition just in case
                return c.getDefaultValue();
            }
            else {
                return value;
            }
        }
    }

    public void writeValue(Config key, String value, long guildId, long userId) throws SQLException {
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
