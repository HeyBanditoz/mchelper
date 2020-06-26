package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GuildConfigDaoImpl extends Dao implements GuildConfigDao {
    private static Cache<Long, GuildConfig> cache = new Cache2kBuilder<Long, GuildConfig>() {
    }
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .disableStatistics(true)
            .build();

    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `guild_config`( `guild_id` bigint(18) NOT NULL, `prefix` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL, `default_channel` bigint(18) DEFAULT NULL, `post_qotd_to_default_channel` tinyint(1) DEFAULT NULL, `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(), PRIMARY KEY (`guild_id`), UNIQUE KEY `default_channel` (`default_channel`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ";
    }

    @Override
    public void saveConfig(GuildConfig config) {
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement("REPLACE INTO `guild_config` VALUES (?, ?, ?, ?, (SELECT NOW()))");
            ps.setLong(1, config.getId());
            ps.setString(2, String.valueOf(config.getPrefix()));
            if (config.getDefaultChannel() == 0) {
                ps.setNull(3, Types.BIGINT);
            }
            else {
                ps.setLong(3, config.getDefaultChannel());
            }
            ps.setBoolean(4, config.getPostQotdToDefaultChannel());
            ps.execute();
            ps.close();
            cache.put(config.getId(), config);
        } catch (SQLException e) {
            LOGGER.error("Error while saving guild configuration for " + config.getId() + "!", e);
        }
    }

    @Override
    public GuildConfig getConfig(Guild g) {
        if (g == null) {
            return new GuildConfig();
        }
        GuildConfig gc = cache.get(g.getIdLong());
        if (gc == null) {
            try (Connection c = Database.getConnection()) {
                PreparedStatement ps = c.prepareStatement("SELECT * FROM `guild_config` WHERE `guild_id` = ?");
                ps.setLong(1, g.getIdLong());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    gc = buildGuildConfigFromResultSet(rs);
                }
                ps.close();

                if (gc != null) {
                    cache.put(gc.getId(), gc);
                    return gc;
                }
                else {
                    return new GuildConfig(g.getIdLong());
                }
            } catch (SQLException e) {
                LOGGER.error("Error while fetching guild configuration for " + g.getId() + "!", e);
            }
        }
        else {
            cache.put(gc.getId(), gc); // refresh expiry times
        }
        return gc;
    }

    @Override
    public List<GuildConfig> getAllGuildConfigs() {
        ArrayList<GuildConfig> guilds = new ArrayList<>();
        try (Connection c = Database.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM `guild_config`");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GuildConfig gc = buildGuildConfigFromResultSet(rs);
                guilds.add(gc);
                cache.put(gc.getId(), gc);
            }
            ps.close();
        } catch (SQLException e) {
            LOGGER.error("Error while fetching all guild configs!", e);
        }
        return guilds;
    }

    @Override
    public int getGuildCount() {
        try (Connection c = Database.getConnection()) {
            ResultSet rs = c.prepareStatement("SELECT COUNT(*) FROM `guild_config`").executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.error("Error while counting the guilds!", e);
        }
        return -1;
    }

    private GuildConfig buildGuildConfigFromResultSet(ResultSet rs) throws SQLException {
        GuildConfig gc = new GuildConfig();
        gc.setId(rs.getLong("guild_id"));
        gc.setPrefix(rs.getString("prefix").charAt(0)); // oh boy
        gc.setDefaultChannel(rs.getLong("default_channel"));
        gc.setPostQotdToDefaultChannel(rs.getBoolean("post_qotd_to_default_channel"));
        gc.setLastModified(rs.getTimestamp("last_modified"));
        return gc;
    }
}
