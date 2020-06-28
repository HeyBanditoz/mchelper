package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public interface GuildConfigDao {
    /**
     * Saves a {@link GuildConfig} to the database.
     *
     * @param config The {@link GuildConfig} to save.
     */
    void saveConfig(GuildConfig config);
    /**
     * Gets a config by the Guild.
     *
     * @param g The {@link GuildConfig}by this {@link Guild} to get.
     * @return The {@link GuildConfig} with the associated guild, or a default object if it does not exist.
     */
    GuildConfig getConfig(Guild g);
    /**
     * Gets all the guild configs.
     *
     * @return A List containing all the guild configs.
     */
    List<GuildConfig> getAllGuildConfigs();
    /**
     * Retrieves the number of guild configs in the database.
     *
     * @return The number of guild configs.
     */
    int getGuildCount();
}
