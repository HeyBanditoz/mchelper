package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.FakeUser;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserCacheDaoImpl extends Dao implements UserCacheDao {
    public UserCacheDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return """
                CREATE TABLE IF NOT EXISTS username_cache (
                    id bigint NOT NULL,
                    username character varying(32) NOT NULL,
                    PRIMARY KEY (id)
                );
                """;
    }

    public void replaceAll(List<User> users) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            PreparedStatement ps = c.prepareStatement("INSERT INTO username_cache VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET username = excluded.username");
            for (User user : users) {
                ps.setLong(1, user.getIdLong());
                ps.setString(2, user.getName());
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
            c.setAutoCommit(true);
            ps.close();
        }
    }

    public void deleteNonexistentUsers(List<User> users) throws SQLException {
        List<FakeUser> dbUsers = getAllCachedUsers();
        // second constructor parameter is null here as we don't need to assign it a value
        List<FakeUser> cachedUsers = users.stream().map(user -> new FakeUser(user.getIdLong(), null)).toList();
        dbUsers.removeAll(cachedUsers);
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            PreparedStatement ps = c.prepareStatement("DELETE FROM username_cache WHERE id = ?");
            for (FakeUser dbUser : dbUsers) {
                ps.setLong(1, dbUser.getId());
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
            c.setAutoCommit(true);
            ps.close();
        }
    }

    public int getCachedUserCount() throws SQLException {
        int userCount;
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM username_cache");
            ResultSet rs = ps.executeQuery();
            rs.next();
            userCount = rs.getInt(1);
            rs.close();
            ps.close();
        }
        return userCount;
    }

    private List<FakeUser> getAllCachedUsers() throws SQLException {
        List<FakeUser> users = new ArrayList<>();
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM username_cache");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(new FakeUser(rs.getLong(1), rs.getString(2)));
            }
            rs.close();
            ps.close();
        }
        return users;
    }
}
