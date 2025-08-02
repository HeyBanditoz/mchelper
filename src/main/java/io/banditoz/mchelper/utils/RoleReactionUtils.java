package io.banditoz.mchelper.utils;

import io.banditoz.mchelper.database.dao.RolesDao;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.awt.Color;
import java.sql.SQLException;
import java.util.List;

public class RoleReactionUtils {
    public static void removeRoleAndUpdateMessage(RolesDao dao, Emoji emoji, Guild guild) throws SQLException {
        ReactionRoleMessage r = dao.getMessageRole(guild);
        if (r != null) {
            Message message = guild.getTextChannelById(r.channelId()).retrieveMessageById(r.messageId()).complete();
            message.clearReactions(emoji).queue();
            MessageEmbed me = new EmbedBuilder().setDescription(buildMessage(dao.getRoles(guild), message)).setColor(Color.CYAN).build();
            message.editMessageEmbeds(me).queue();
        }
    }

    public static String buildMessage(List<ReactionRole> roles, Message message) {
        StringBuilder sb = new StringBuilder("React to be added to a role, remove your reaction to lose the role.\n");
        for (ReactionRole r : roles) {
            sb.append("\n").append(r.emoji().getFormatted()).append(" ").append(r.name());
            message.addReaction(r.emoji()).queue();
        }
        return sb.toString();
    }
}
