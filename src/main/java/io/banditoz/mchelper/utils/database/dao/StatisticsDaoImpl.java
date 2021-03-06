package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.StatPoint;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsDaoImpl extends Dao implements StatisticsDao {
    public StatisticsDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `statistics`( `guild_id` bigint(18) DEFAULT NULL, `channel_id` bigint(18) DEFAULT NULL, `author_id` bigint(18) NOT NULL, `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL, `arguments` varchar(1997) COLLATE utf8mb4_unicode_ci NOT NULL, `return_code` tinyint(4) NOT NULL, `execution_time` int(11) NOT NULL, `executed_at` datetime NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    @Override
    public void log(Stat s) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO `statistics` VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            if (s.getEvent().isFromGuild()) {
                ps.setLong(1, s.getEvent().getGuild().getIdLong());
            }
            else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setLong(2, s.getEvent().getChannel().getIdLong());
            ps.setLong(3, s.getEvent().getAuthor().getIdLong());
            ps.setString(4, s.getClassName());
            ps.setString(5, s.getArgs());
            ps.setInt(6, s.getStatus().getValue());
            ps.setInt(7, s.getExecutionTime());
            ps.setTimestamp(8, Timestamp.valueOf(s.getExecutedWhen()));
            ps.execute();
        }
    }

    @Override
    public List<StatPoint<String, Integer>> getUniqueCommandCountPerGuildOrGlobally(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps;
            if (g == null) {
                ps = c.prepareStatement("SELECT name, COUNT(name) AS 'count' FROM `statistics` GROUP BY name ORDER BY COUNT(name) DESC;");
            }
            else {
                ps = c.prepareStatement("SELECT name, COUNT(name) AS 'count' FROM `statistics` WHERE guild_id=? GROUP BY name ORDER BY COUNT(name) DESC;");
                ps.setLong(1, g.getIdLong());
            }
            ps.execute();
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isLast()) {
                    ArrayList<StatPoint<String, Integer>> stats = new ArrayList<>();
                    while (rs.next()) {
                        stats.add(new StatPoint<>(rs.getString("name"), rs.getInt("count")));
                    }
                    stats.sort(StatPoint::compareTo);
                    return stats;
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getTotalCommandsRun() throws SQLException {
        int commandsRun = -1;
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM statistics WHERE name LIKE '%Command%';");
            ResultSet rs = ps.executeQuery();
            rs.next();
            commandsRun = rs.getInt(1);
            rs.close();
            ps.close();
        }
        return commandsRun;
    }
}
