package io.banditoz.mchelper.database.dao;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;

import io.banditoz.mchelper.utils.ReactionRole;
import io.banditoz.mchelper.utils.ReactionRoleMessage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public interface RolesDao {
    void init(TextChannel channel, Message message, Guild g) throws SQLException;
    List<Long> getRoleReactions() throws SQLException;
    @Nullable
    ReactionRole getByEmote(Emoji emoji, Guild g) throws SQLException;
    void deactivate(Guild g) throws SQLException;
    boolean containsGuild(Guild g) throws SQLException;
    @Nullable
    ReactionRoleMessage getMessageRole(Guild g) throws SQLException;
    void addRole(Emoji emoji, String name, Guild g, Role r) throws SQLException;
    List<ReactionRole> getRoles(Guild g) throws SQLException;
    int getRoleCount(Guild g) throws SQLException;
    boolean guildContainsName(Guild g, String name) throws SQLException;
    Emoji removeRole(Guild g, String name) throws SQLException;
    Emoji removeRole(Role r) throws SQLException;
}
