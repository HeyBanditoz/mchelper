package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;

import java.sql.Connection;
import java.sql.SQLException;

public class LevelDaoImpl extends Dao implements LevelDao {
    public LevelDaoImpl(Database database) {
        super(database);
    }

    @Override
    public int incrementUser(long userId, boolean passedSpamCheck) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            if (passedSpamCheck) {
                Query.of("""
                        INSERT INTO levels (user_id, messages_sent, levelable_messages_sent)
                        VALUES (:u, 1, 1)
                        ON CONFLICT (user_id) DO UPDATE SET
                            messages_sent = levels.messages_sent + 1,
                            levelable_messages_sent = levels.levelable_messages_sent + 1
                        """)
                        .on(Param.value("u", userId))
                        .executeUpdate(c);
            }
            else {
                Query.of("""
                        INSERT INTO levels (user_id, messages_sent, levelable_messages_sent)
                        VALUES (:u, 1, 0)
                        ON CONFLICT (user_id) DO UPDATE SET
                            messages_sent = levels.messages_sent + 1,
                            levelable_messages_sent = levels.levelable_messages_sent
                        """)
                        .on(Param.value("u", userId))
                        .executeUpdate(c);
            }
            return Query.of("SELECT levelable_messages_sent FROM levels WHERE user_id = :u")
                    .on(Param.value("u", userId))
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }

    @Override
    public int getLevel(long userId) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT level FROM levels WHERE user_id = :u")
                    .on(Param.value("u", userId))
                    .as((rs, conn) -> {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                        else {
                            return 0;
                        }
                    }, c);
        }
    }

    @Override
    public void levelUp(long userId) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("UPDATE levels SET level = level + 1 WHERE user_id = :u")
                    .on(Param.value("u", userId))
                    .executeUpdate(c);
        }
    }
}
