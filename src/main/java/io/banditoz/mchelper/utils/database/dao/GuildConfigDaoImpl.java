package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuildConfigDaoImpl extends Dao implements GuildConfigDao {
    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `guild_config`( `guild_id` bigint(18) NOT NULL, `prefix` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL, `default_channel` bigint(18) DEFAULT NULL, `post_qotd_to_default_channel` tinyint(1) DEFAULT NULL, `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(), PRIMARY KEY (`guild_id`), UNIQUE KEY `default_channel` (`default_channel`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ";
    }

    @Override
    public void saveConfig(GuildConfig config) {
        try {
            PreparedStatement ps = connection.prepareStatement("REPLACE INTO `guild_config` VALUES (?, ?, ?, ?, (SELECT NOW()))");
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
        } catch (SQLException e) {
            LOGGER.error("Error while saving guild configuration for " + config.getId() + "!", e);
        }
    }

    @Override
    public GuildConfig getConfig(Guild g) {
        GuildConfig gc = new GuildConfig(g.getIdLong());
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `guild_config` WHERE `guild_id` = ?");
            ps.setLong(1, g.getIdLong());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                gc = buildGuildConfigFromResultSet(rs);
            }
            ps.close();
            return gc;
        } catch (SQLException e) {
            LOGGER.error("Error while fetching guild configuration for " + g.getId() + "!", e);
        }
        return gc;
    }

    @Override
    public List<GuildConfig> getAllGuildConfigs() {
        ArrayList<GuildConfig> guilds = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `guild_config`");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                guilds.add(buildGuildConfigFromResultSet(rs));
            }
            ps.close();
        } catch (SQLException e) {
            LOGGER.error("Error while fetching all guild configs!", e);
        }
        return guilds;
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
