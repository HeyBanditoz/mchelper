package io.banditoz.mchelper;

import jakarta.inject.Singleton;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SelfGuildJoinLeaveListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfGuildJoinLeaveListener.class);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        LOGGER.info("We joined guild " + event.getGuild());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        LOGGER.info("We left guild " + event.getGuild());
    }
}
