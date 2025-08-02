package io.banditoz.mchelper;

import io.banditoz.mchelper.config.GuildConfigurationProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

/**
 * Interface representing something a user could run.
 */
public interface UserEvent {
    Guild getGuild();
    GuildConfigurationProvider getConfig();
    User getUser();
    long getUserId();
    String commandName();
}
