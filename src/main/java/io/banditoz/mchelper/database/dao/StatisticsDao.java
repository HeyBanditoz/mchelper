package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;
import java.util.List;

import io.banditoz.mchelper.database.StatPoint;
import io.banditoz.mchelper.stats.Stat;
import net.dv8tion.jda.api.entities.Guild;

public interface StatisticsDao {
    void log(Stat s) throws SQLException;
    List<StatPoint<String>> getUniqueCommandCountPerGuildOrGlobally(Guild g) throws SQLException;
    int getTotalCommandsRun() throws SQLException;
}
