package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.ReactionRole;
import io.banditoz.mchelper.utils.ReactionRoleMessage;
import io.banditoz.mchelper.utils.database.Database;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolesDaoImpl extends Dao implements RolesDao {
    public RolesDaoImpl(Database database) {
        super(database);
    }

    @Override
    public void init(@NotNull TextChannel channel, @NotNull Message message, @NotNull Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("INSERT INTO guild_roles (guild_id, channel_id, message_id) VALUES (:g, :c, :m)")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.value("c", channel.getIdLong()),
                            Param.value("m", message.getIdLong())
                    ).executeUpdate(c);
        }
    }

    @Override
    public List<Long> getRoleReactions() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT message_id FROM guild_roles")
                    .as((rs, conn) -> {
                        List<Long> messages = new ArrayList<>();
                        while (rs.next()) {
                            messages.add(rs.getLong("message_id"));
                        }
                        return messages;
                    }, c);
        }
    }

    @Override
    public ReactionRole getByEmote(Emoji emoji, Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM roles WHERE emote=:e AND guild_id=:g")
                    .on(
                            Param.value("e", emoji.getFormatted()),
                            Param.value("g", g.getIdLong())
                    ).as(this::parseOneReactionRole, c);
        }
    }

    @Override
    public void deactivate(@NotNull Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            Query.of("DELETE FROM roles WHERE guild_id=:g")
                    .on(Param.value("g", g.getIdLong()))
                    .executeUpdate(c);
            Query.of("DELETE FROM guild_roles WHERE guild_id=:g")
                    .on(Param.value("g", g.getIdLong()))
                    .executeUpdate(c);
            c.commit();
            c.setAutoCommit(true);
        }
    }

    @Override
    public boolean containsGuild(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT 1 FROM guild_roles WHERE guild_id=:g LIMIT 1")
                    .on(Param.value("g", g.getIdLong()))
                    .as((rs, conn) -> rs.next(), c);
        }
    }

    @Override
    public ReactionRoleMessage getMessageRole(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM guild_roles WHERE guild_id=:g LIMIT 1")
                    .on(Param.value("g", g.getIdLong()))
                    .as(this::parseOneReactionRoleMessage, c);
        }
    }

    @Override
    public void addRole(Emoji emoji, String name, Guild g, Role r) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("INSERT INTO roles (guild_id, emote, name, role_id) VALUES (:g, :e, :n, :r)")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.value("e", emoji.getFormatted()),
                            Param.value("n", name),
                            Param.value("r", r.getIdLong())
                    ).executeUpdate(c);
        }
    }

    @Override
    public List<ReactionRole> getRoles(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM roles WHERE guild_id=:g")
                    .on(Param.value("g", g.getIdLong()))
                    .as(this::parseManyReactionRoles, c);
        }
    }

    @Override
    public int getRoleCount(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT COUNT(*) FROM roles WHERE guild_id=:g")
                    .on(Param.value("g", g.getIdLong()))
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }

    @Override
    public boolean guildContainsName(Guild g, String name) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT 1 FROM roles WHERE guild_id=:g AND NAME=:n")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.value("n", name)
                    ).as((rs, conn) -> rs.next(), c);
        }
    }

    @Override
    public Emoji removeRoleAndReturnEmoji(Guild g, String name) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Emoji e = Query.of("SELECT emote FROM roles WHERE guild_id=:g AND name=:n")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.value("n", name)
                    )
                    .as((rs, conn) -> {
                        // should never have to check next
                        rs.next();
                        return Emoji.fromFormatted(rs.getString("emote"));
                    }, c);
            Query.of("DELETE FROM roles WHERE guild_id=:g AND name=:n")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.value("n", name)
                    ).executeUpdate(c);
            return e;
        }
    }

    private @Nullable ReactionRole parseOneReactionRole(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return new ReactionRole(
                rs.getInt("id"),
                rs.getLong("guild_id"),
                rs.getString("emote"),
                rs.getString("name"),
                rs.getLong("role_id")
        );
    }

    private @Nullable ReactionRoleMessage parseOneReactionRoleMessage(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return new ReactionRoleMessage(
                rs.getLong("id"),
                rs.getLong("guild_id"),
                rs.getLong("channel_id"),
                rs.getLong("message_Id")
        );
    }

    private List<ReactionRole> parseManyReactionRoles(ResultSet rs, Connection c) throws SQLException {
        List<ReactionRole> roles = new ArrayList<>();
        while (!rs.isLast()) {
            ReactionRole rr = parseOneReactionRole(rs, c);
            if (rr != null) {
                roles.add(rr);
            }
            else {
                break;
            }
        }
        return roles;
    }
}
