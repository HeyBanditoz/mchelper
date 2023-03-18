package io.banditoz.mchelper;

import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.time.Instant;

public class GuildJoinLeaveListener extends ListenerAdapter {
    private static final String DEFAULT_AVATAR = "https://discordapp.com/assets/322c936a8c8be1b803cd94861bdfa868.png";
    private static final Logger log = LoggerFactory.getLogger(GuildJoinLeaveListener.class);
    private final ConfigurationProvider gc;

    public GuildJoinLeaveListener(Database database) {
        this.gc = new ConfigurationProvider(database);
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        log.info("[{}] {} joined {}", (event.getUser().isBot() ? "Bot" : "User"), event.getUser(), event.getGuild());
        String defaultChannel = gc.getValue(Config.DEFAULT_CHANNEL, event.getGuild().getIdLong());
        if (!defaultChannel.equals("0")) {
            MessageEmbed me = new EmbedBuilder()
                    .setTitle((event.getUser().isBot() ? "Bot" : "User") + " joined the guild.")
                    .setThumbnail(event.getUser().getAvatarUrl() == null ? DEFAULT_AVATAR : event.getUser().getAvatarUrl())
                    .setColor(Color.GREEN)
                    .setDescription(getNameAndDiscriminator(event.getUser()))
                    .setFooter(event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .build();
            event.getGuild().getTextChannelById(defaultChannel).sendMessageEmbeds(me).queue();
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        log.info("[{}] {} left {}", (event.getUser().isBot() ? "Bot" : "User"), event.getUser(), event.getGuild());
        String defaultChannel = gc.getValue(Config.DEFAULT_CHANNEL, event.getGuild().getIdLong());
        if (!defaultChannel.equals("0")) {
            MessageEmbed me = new EmbedBuilder()
                    .setTitle((event.getUser().isBot() ? "Bot" : "User") + " left the guild.")
                    .setThumbnail(event.getUser().getAvatarUrl() == null ? DEFAULT_AVATAR : event.getUser().getAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription(getNameAndDiscriminator(event.getUser()))
                    .setFooter(event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .build();
            event.getGuild().getTextChannelById(defaultChannel).sendMessageEmbeds(me).queue();
        }
    }

    private String getNameAndDiscriminator(User u) {
        return u.getName() + "#" + u.getDiscriminator();
    }
}
