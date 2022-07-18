package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.RoleObject;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RolesDaoImpl extends Dao implements RolesDao {
    public RolesDaoImpl(Database database) {
        super(database);
    }

    @Override
    public void init(@NotNull TextChannel channel, @NotNull Message message, @NotNull Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO guild_roles (guild_id, channel_id, message_id) VALUES (?, ?, ?)");
            ps.setLong(1, g.getIdLong());
            ps.setLong(2, channel.getIdLong());
            ps.setLong(3, message.getIdLong());
            ps.execute();
            ps.close();
        }
    }

    @Override
    public Map.Entry<String,String> deactivate(@NotNull Guild g) throws SQLException {
        Map.Entry<String,String> id = null;
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM guild_roles WHERE guild_id=?");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                id = new AbstractMap.SimpleEntry<>(rs.getString("channel_id"),rs.getString("message_id"));
            }
            PreparedStatement ps2 = c.prepareStatement("DELETE FROM roles WHERE guild_id = ?");
            ps2.setLong(1, g.getIdLong());
            ps2.execute();
            ps2.close();
            PreparedStatement ps3 = c.prepareStatement("DELETE FROM guild_roles WHERE guild_id = ?");
            ps3.setLong(1, g.getIdLong());
            ps3.execute();
            ps3.close();
        }
        return id;
    }

    @Override
    public boolean addRole(String emote, String name, @NotNull Guild g, Role role) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps2 = c.prepareStatement("SELECT * FROM roles WHERE guild_id=? AND emote = ? OR roles.name = ? OR role_id = ?");
            ps2.setLong(1, g.getIdLong());
            ps2.setString(2, emote);
            ps2.setString(3, name);
            ps2.setLong(4, role.getIdLong());
            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) return false;
            }
            PreparedStatement ps = c.prepareStatement("INSERT INTO roles (guild_id, emote, name, role_id) VALUES (?, ?, ?, ?)");
            ps.setLong(1, g.getIdLong());
            ps.setString(2, emote);
            ps.setString(3, name);
            ps.setLong(4, role.getIdLong());
            ps.execute();
            ps.close();
            return true;
        }
    }

    @Override
    public String removeRole(String name, @NotNull Guild g) throws SQLException {
        String s = "";
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps2 = c.prepareStatement("SELECT * FROM roles WHERE guild_id=? AND roles.name=?");
            ps2.setLong(1, g.getIdLong());
            ps2.setString(2, name);
            try (ResultSet rs = ps2.executeQuery()) {
                rs.next();
                s = rs.getString("emote");
            }
            PreparedStatement ps = c.prepareStatement("DELETE FROM roles WHERE roles.name = ? AND guild_id = ?");
            ps.setString(1, name);
            ps.setLong(2, g.getIdLong());
            ps.execute();
            ps.close();
        }
        return s;
    }

    @Override
    public boolean containsGuild(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM guild_roles WHERE guild_id=? LIMIT 1");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                boolean b = rs.next();
                ps.close();
                return b;
            }
        }
    }

    @Override
    public boolean containsName(String n, Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM roles WHERE guild_id=? AND name = ?");
            ps.setLong(1, g.getIdLong());
            ps.setString(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                ps.close();
                return true;
            }
        }
    }

    @Override
    public RoleObject getRoleByEmote(Guild g, String emote) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM roles WHERE guild_id=? AND emote=?");
            ps.setLong(1, g.getIdLong());
            ps.setString(2,emote);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new RoleObject(rs.getString("emote"),rs.getString("name"),rs.getString("role_id"));
            }
        }
    }

    @Override
    public List<RoleObject> getRoles(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM roles WHERE guild_id=?");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                List<RoleObject> hm = new ArrayList<>();
                while (rs.next()) {
                    hm.add(new RoleObject(rs.getString("emote"),rs.getString("name"),rs.getString("role_id")));
                }
                ps.close();
                rs.close();
                return hm;
            }
        }
    }

    @Override
    public Map.Entry<String, String> getChannelAndMessageId(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM guild_roles WHERE guild_id=?");
            ps.setLong(1, g.getIdLong());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new AbstractMap.SimpleEntry<>(rs.getString("channel_id"), rs.getString("message_id"));
            }
        }
    }
}
