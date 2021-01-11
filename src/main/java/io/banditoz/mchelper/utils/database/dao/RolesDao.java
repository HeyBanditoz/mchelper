package io.banditoz.mchelper.utils.database.dao;


import io.banditoz.mchelper.utils.RoleObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface RolesDao {
    void init(@NotNull TextChannel channel, @NotNull Message message, @NotNull Guild g) throws SQLException;
    Map.Entry<String,String> deactivate(@NotNull Guild g) throws SQLException;
    boolean addRole(String emote, String name, @NotNull Guild g, Role role) throws SQLException;
    String removeRole(String name, @NotNull Guild g) throws SQLException;
    boolean containsGuild(Guild g) throws SQLException;
    boolean containsName(String n, Guild g) throws SQLException;
    RoleObject getRoleByEmote(Guild g, String emote) throws SQLException;
    List<RoleObject> getRoles(Guild g) throws SQLException;
    Map.Entry<String, String> getChannelAndMessageId(Guild g) throws SQLException;
}
