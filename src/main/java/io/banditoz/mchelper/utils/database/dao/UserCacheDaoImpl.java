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
import java.util.stream.Collectors;

public class UserCacheDaoImpl extends Dao implements UserCacheDao {
    public UserCacheDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `username_cache`( `id` bigint(18) NOT NULL, `username` varchar(32) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1";
    }

    public void replaceAll(List<User> users) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            for (User user : users) {
                PreparedStatement ps = c.prepareStatement("REPLACE INTO username_cache VALUES (?, ?)");
                ps.setLong(1, user.getIdLong());
                ps.setString(2, user.getName());
                ps.execute();
                ps.close();
            }
        }
    }

    public void deleteNonexistentUsers(List<User> users) throws SQLException {
        List<FakeUser> dbUsers = getAllCachedUsers();
        // second constructor parameter is null here as we don't need to assign it a value
        List<FakeUser> cachedUsers = users.stream().map(user -> new FakeUser(user.getIdLong(), null)).collect(Collectors.toList());
        dbUsers.removeAll(cachedUsers);
        try (Connection c = DATABASE.getConnection()) {
            for (FakeUser dbUser : dbUsers) {
                PreparedStatement ps = c.prepareStatement("DELETE FROM username_cache WHERE id = ?");
                ps.setLong(1, dbUser.getId());
                ps.execute();
                ps.close();
            }
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
