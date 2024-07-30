package io.banditoz.mchelper;

import io.banditoz.mchelper.config.GuildConfigurationProvider;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

/**
 * Interface representing something a user could run.
 */
public interface UserEvent {
    Guild getGuild();
    Database getDatabase();
    GuildConfigurationProvider getConfig();
    MCHelper getMCHelper();
    User getUser();
    long getUserId();
    String commandName();
}
