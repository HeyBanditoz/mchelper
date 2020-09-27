package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.stats.Stat;

import java.sql.SQLException;

public interface StatisticsDao {
    void log(Stat s) throws SQLException;
}
