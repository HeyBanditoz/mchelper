package io.banditoz.mchelper.jda;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

@Singleton
public class GuildMessageChannelResolver {
    private final JDA jda;

    @Inject
    public GuildMessageChannelResolver(JDA jda) {
        this.jda = jda;
    }

    /**
     * @return The {@link GuildMessageChannel} by ID, which is capable of potentially having a message delivered to it.
     * It may a thread, or a regular text channel.
     * @apiNote This uses JDA's caching mechanisms.
     */
    public GuildMessageChannel getGuildMessageChannelById(long id) {
        GuildMessageChannel channel = jda.getThreadChannelById(id);
        if (channel == null) {
            channel = jda.getTextChannelById(id);
        }
        return channel;
    }

    public GuildMessageChannel getGuildMessageChannelById(String id) {
        return getGuildMessageChannelById(Long.parseLong(id));
    }
}
