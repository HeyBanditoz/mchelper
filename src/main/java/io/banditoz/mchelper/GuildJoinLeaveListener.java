package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.time.Instant;

public class GuildJoinLeaveListener extends ListenerAdapter {
    private static String DEFAULT_AVATAR = "https://discordapp.com/assets/322c936a8c8be1b803cd94861bdfa868.png";

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        GuildData gd = Database.getInstance().getGuildDataNull(event.getGuild());
        if (!(gd == null)) {
            MessageEmbed me = new EmbedBuilder()
                    .setTitle((event.getUser().isBot() ? "Bot" : "User") + " joined the guild.")
                    .setThumbnail(event.getUser().getAvatarUrl() == null ? DEFAULT_AVATAR : event.getUser().getAvatarUrl())
                    .setColor(Color.GREEN)
                    .setDescription(getNameAndDiscriminator(event.getUser()))
                    .setFooter(event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .build();
            event.getGuild().getTextChannelById(gd.getDefaultChannel()).sendMessage(me).queue();
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        GuildData gd = Database.getInstance().getGuildDataNull(event.getGuild());
        if (!(gd == null)) {
            MessageEmbed me = new EmbedBuilder()
                    .setTitle((event.getUser().isBot() ? "Bot" : "User") + " left the guild.")
                    .setThumbnail(event.getUser().getAvatarUrl() == null ? DEFAULT_AVATAR : event.getUser().getAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription(getNameAndDiscriminator(event.getUser()))
                    .setFooter(event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .build();
            event.getGuild().getTextChannelById(gd.getDefaultChannel()).sendMessage(me).queue();
        }
    }

    private String getNameAndDiscriminator(User u) {
        return u.getName() + "#" + u.getDiscriminator();
    }
}
