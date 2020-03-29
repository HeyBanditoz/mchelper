package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class GuildJoinLeaveListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        GuildData gd = Database.getInstance().getGuildDataNull(event.getGuild());
        if (!(gd == null)) {
            CommandUtils.sendReply(getNameAndDiscriminator(event.getUser()) + " has joined the guild.", event.getGuild().getTextChannelById(gd.getDefaultChannel()));
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        GuildData gd = Database.getInstance().getGuildDataNull(event.getGuild());
        if (!(gd == null)) {
            CommandUtils.sendReply(getNameAndDiscriminator(event.getUser()) + " has left the guild.", event.getGuild().getTextChannelById(gd.getDefaultChannel()));
        }
    }

    private String getNameAndDiscriminator(User u) {
        return u.getName() + "#" + u.getDiscriminator() + " (" + u.getId() + ")";
    }
}
