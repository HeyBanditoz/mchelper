package io.banditoz.mchelper.utils.database.dao;

import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;
import java.util.List;

public interface UserCacheDao {
    void replaceAll(List<User> users) throws SQLException;
    void deleteNonexistentUsers(List<User> users) throws SQLException;
    int getCachedUserCount() throws SQLException;
}
