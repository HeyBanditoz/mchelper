package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;
import java.util.List;

import net.dv8tion.jda.api.entities.User;

public interface UserCacheDao {
    void replaceAll(List<User> users) throws SQLException;
    void deleteNonexistentUsers(List<User> users) throws SQLException;
    int getCachedUserCount() throws SQLException;
}
