package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StatisticsDaoImpl extends Dao implements StatisticsDao {
    public StatisticsDaoImpl(Database database) {
        super(database);
    }

    @Override
    public void log(Stat s) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("INSERT INTO statistics VALUES (:a, :b, :c, :d, :e, :f, :g, :h)")
                    .on(
                            Param.value("a", s.getEvent().isFromGuild() ? s.getEvent().getGuild().getIdLong() : Optional.empty()),
                            Param.value("b", s.getEvent().getChannel().getIdLong()),
                            Param.value("c", s.getEvent().getAuthor().getIdLong()),
                            Param.value("d", s.getClassName()),
                            Param.value("e", s.getArgs()),
                            Param.value("f", s.getStatus().getValue()),
                            Param.value("g", s.getExecutionTime()),
                            Param.value("h", Timestamp.valueOf(s.getExecutedWhen()))
                    ).execute(c);
        }
    }

    @Override
    public List<StatPoint<String>> getUniqueCommandCountPerGuildOrGlobally(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query q;
            if (g == null) {
                q = Query.of("SELECT name, COUNT(name) AS \"count\" FROM statistics GROUP BY name ORDER BY COUNT(name) DESC");
            }
            else {
                q = Query.of("SELECT name, COUNT(name) AS \"count\" FROM statistics WHERE guild_id=:g GROUP BY name ORDER BY COUNT(name) DESC;")
                        .on(Param.value("g", g.getIdLong()));
            }
            return q.as((rs, conn) -> {
                if (!rs.isLast()) {
                    ArrayList<StatPoint<String>> stats = new ArrayList<>();
                    while (rs.next()) {
                        stats.add(new StatPoint<>(rs.getString("name"), rs.getInt("count")));
                    }
                    stats.sort(StatPoint::compareTo);
                    return stats;
                }
                return Collections.emptyList();
            }, c);
        }
    }

    @Override
    public int getTotalCommandsRun() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT COUNT(*) FROM statistics WHERE name LIKE '%Command%';")
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }
}
