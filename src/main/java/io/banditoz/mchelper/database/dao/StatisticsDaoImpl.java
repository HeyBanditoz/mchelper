package io.banditoz.mchelper.database.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.database.StatPoint;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Stat;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@Singleton
@RequiresDatabase
public class StatisticsDaoImpl extends Dao implements StatisticsDao {
    @Inject
    public StatisticsDaoImpl(Database database) {
        super(database);
    }

    @Override
    public void log(Stat s) throws SQLException {
        Channel channel = s.getChannel();
        try (Connection c = database.getConnection()) {
            Query.of("INSERT INTO statistics VALUES (:a, :b, :t, :c, :d, :e, :f, :k, :g, :h)")
                    .on(
                            Param.value("a", channel instanceof GuildChannel gc ? gc.getGuild().getIdLong() : Optional.empty()),
                            Param.value("b", channel instanceof ThreadChannel t ? t.getParentChannel().getIdLong() : channel.getIdLong()),
                            Param.value("t", (channel instanceof ThreadChannel t ? t.getIdLong() : Optional.empty())),
                            Param.value("c", s.getUser().getIdLong()),
                            Param.value("d", s.getClassName()),
                            Param.value("e", s.getArgs()),
                            Param.value("f", s.getStatus().getValue()),
                            Param.value("k", s.getKind().ordinal()),
                            Param.value("g", s.getExecutionTime()),
                            Param.value("h", Timestamp.valueOf(s.getExecutedWhen()))
                    ).execute(c);
        }
    }

    @Override
    public List<StatPoint<String>> getUniqueCommandCountPerGuildOrGlobally(Guild g) throws SQLException {
        try (Connection c = database.getConnection()) {
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
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT COUNT(*) FROM statistics WHERE name LIKE '%Command%';")
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }
}
