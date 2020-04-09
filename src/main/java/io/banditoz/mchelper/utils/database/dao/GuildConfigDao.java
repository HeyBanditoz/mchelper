package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.GuildConfig;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public interface GuildConfigDao {
    void saveConfig(GuildConfig config);
    GuildConfig getConfig(Guild g);
    List<GuildConfig> getAllGuildConfigs();
    int getGuildCount();
}
