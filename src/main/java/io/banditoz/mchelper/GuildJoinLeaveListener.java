package io.banditoz.mchelper;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.time.Instant;

import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GuildJoinLeaveListener extends ListenerAdapter {
    private static final String DEFAULT_AVATAR = "https://discordapp.com/assets/322c936a8c8be1b803cd94861bdfa868.png";
    private static final Logger log = LoggerFactory.getLogger(GuildJoinLeaveListener.class);
    private final ConfigurationProvider gc;

    @Inject
    public GuildJoinLeaveListener(ConfigurationProvider configurationProvider) {
        this.gc = configurationProvider;
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        log.info("[{}] {} joined {}", (event.getUser().isBot() ? "Bot" : "User"), event.getUser(), event.getGuild());
        String defaultChannel = gc.getValue(Config.DEFAULT_CHANNEL, event.getGuild());
        if (defaultChannel != null) {
            MessageEmbed me = new EmbedBuilder()
                    .setTitle((event.getUser().isBot() ? "Bot" : "User") + " joined the guild.")
                    .setThumbnail(event.getUser().getAvatarUrl() == null ? DEFAULT_AVATAR : event.getUser().getAvatarUrl())
                    .setColor(Color.GREEN)
                    .setDescription(event.getMember().getEffectiveName())
                    .setFooter(event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .build();
            event.getGuild().getTextChannelById(defaultChannel).sendMessageEmbeds(me).queue();
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        log.info("[{}] {} left {}", (event.getUser().isBot() ? "Bot" : "User"), event.getUser(), event.getGuild());
        String defaultChannel = gc.getValue(Config.DEFAULT_CHANNEL, event.getGuild());
        if (defaultChannel != null) {
            MessageEmbed me = new EmbedBuilder()
                    .setTitle((event.getUser().isBot() ? "Bot" : "User") + " left the guild.")
                    .setThumbnail(event.getUser().getAvatarUrl() == null ? DEFAULT_AVATAR : event.getUser().getAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription(event.getMember() == null ? event.getUser().getEffectiveName() : event.getMember().getEffectiveName())
                    .setFooter(event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .build();
            event.getGuild().getTextChannelById(defaultChannel).sendMessageEmbeds(me).queue();
        }
    }
}
