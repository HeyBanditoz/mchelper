package io.banditoz.mchelper.utils.database.dao;

import java.sql.SQLException;

public interface LevelDao {
    /**
     * @return Number of levelable messages this user has.
     */
    int incrementUser(long userId, boolean passedSpamCheck) throws SQLException;
    int getLevel(long userId) throws SQLException;
    void levelUp(long idLong) throws SQLException;
}
