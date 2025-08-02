package io.banditoz.mchelper.database.dao;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.database.Lottery;
import io.banditoz.mchelper.database.LotteryEntrant;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Singleton
@RequiresDatabase
public class LotteryDaoImpl extends Dao implements LotteryDao {
    @Inject
    public LotteryDaoImpl(Database database) {
        super(database);
    }

    @Override
    public Lottery getActiveLottery(Guild g) throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT * FROM lottery WHERE guild_id = :g AND complete = false LIMIT 1")
                    .on(Param.value("g", g.getIdLong()))
                    .as(this::parseOne, c);
        }
    }

    @Override
    public List<Lottery> getAllActiveLotteries() throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("""
                            SELECT * FROM lottery WHERE complete = false AND
                            (SELECT COUNT(*) FROM lottery_entrants WHERE lottery_id = id) > 1""")
                    .as((rs, conn) -> parseMany(rs, conn, this::parseOne), c);
        }
    }

    public boolean memberInCurrentLottery(Member m) throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT 1 FROM lottery_entrants e INNER JOIN lottery l on l.id = e.lottery_id WHERE guild_id = :g AND author_id = :m AND complete = false LIMIT 1")
                    .on(
                            Param.value("g", m.getGuild().getIdLong()),
                            Param.value("m", m.getIdLong())
                    )
                    .as((rs, conn) -> rs.next(), c);
        }
    }

    @Override
    public void createLottery(TextChannel channel, BigDecimal max) throws SQLException {
        Guild g = channel.getGuild();
        if (getActiveLottery(g) != null) {
            // this case shouldn't happen
            throw new IllegalStateException("Guild " + g + " already has an active lottery.");
        }
        try (Connection c = database.getConnection()) {
            Query.of("INSERT INTO lottery (guild_id, \"limit\", channel_id, draw_at) VALUES (:g, :l, :c, :d)")
                    .on(
                            Param.value("g", g.getIdLong()),
                            Param.value("l", max),
                            Param.value("c", channel.getIdLong()),
                            Param.value("d", Timestamp.from(Instant.now().plus(4, ChronoUnit.HOURS)))
                    ).executeUpdate(c);
        }
    }

    @Override
    public void enterLottery(Member m, BigDecimal amount) throws SQLException {
        Guild g = m.getGuild();
        Lottery l = getActiveLottery(g);
        if (l == null) {
            throw new IllegalStateException("Guild " + g + " does not have a lottery.");
        }
        if (memberInCurrentLottery(m)) {
            throw new IllegalStateException("You are already in the lottery for this guild.");
        }
//        if (amount.compareTo(l.limit()) > 0) {
//            throw new IllegalArgumentException("Requested ticket of $" + format(amount) + " breaches lottery limit of $" + format(l.limit()));
//        }
        try (Connection c = database.getConnection()) {
            Query.of("INSERT INTO lottery_entrants (lottery_id, author_id, amount) VALUES (:l, :a, :m)")
                    .on(
                            Param.value("l", l.id()),
                            Param.value("a", m.getIdLong()),
                            Param.value("m", amount)
                    ).executeUpdate(c);
        }
    }

    @Override
    public List<LotteryEntrant> getEntrantsForLottery(Guild g) throws SQLException {
        Lottery l = getActiveLottery(g);
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT author_id, amount FROM lottery_entrants e INNER JOIN lottery l on l.id = e.lottery_id WHERE lottery_id = :l AND complete = false ORDER BY amount DESC")
                    .on(Param.value("l", l.id()))
                    .as((rs, conn) -> parseMany(rs, conn, this::parseOneEntrant), c);
        }
    }

    @Override
    public void markLotteryComplete(long lotteryId) throws SQLException {
        try (Connection c = database.getConnection()) {
            Query.of("UPDATE lottery SET complete = true WHERE id = :l")
                    .on(Param.value("l", lotteryId))
                    .executeUpdate(c);
        }
    }

    @Override
    public int countParticipantsForLottery(long lotteryId) throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT COUNT(*) FROM lottery_entrants WHERE lottery_id = :l")
                    .on(Param.value("l", lotteryId))
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }

    private @Nullable Lottery parseOne(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return new Lottery(
                rs.getInt("id"),
                rs.getLong("guild_id"),
                rs.getLong("channel_id"),
                rs.getBigDecimal("limit"),
                rs.getTimestamp("draw_at"),
                rs.getBoolean("complete")
        );
    }

    private @Nullable LotteryEntrant parseOneEntrant(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return new LotteryEntrant(
                rs.getLong("author_id"),
                rs.getBigDecimal("amount")
        );
    }
}
