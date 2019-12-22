package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildData;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class GuildJoinLeaveListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        GuildData gd = Database.getInstance().getGuildDataNull(event.getGuild());
        if (!(gd == null)) {
            CommandUtils.sendReply(event.getMember().getUser().getName() + " has joined the guild.", event.getGuild().getTextChannelById(gd.getDefaultChannel()));
        }
    }

    @Override
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        GuildData gd = Database.getInstance().getGuildDataNull(event.getGuild());
        if (!(gd == null)) {
            CommandUtils.sendReply(event.getMember().getUser().getName() + " has left the guild.", event.getGuild().getTextChannelById(gd.getDefaultChannel()));
        }
    }
}
