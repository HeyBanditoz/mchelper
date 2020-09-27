package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.utils.database.StatPoint;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.Set;

public interface StatisticsDao {
    void log(Stat s) throws SQLException;
    Set<StatPoint<String>> getUniqueCommandCountPerGuildOrGlobally(Guild g) throws SQLException;
}
