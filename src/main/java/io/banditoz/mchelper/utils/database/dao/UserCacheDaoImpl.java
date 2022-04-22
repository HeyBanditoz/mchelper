package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.FakeUser;
import io.jenetics.facilejdbc.Batch;
import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserCacheDaoImpl extends Dao implements UserCacheDao {
    private static final Dctor<User> DCTOR = Dctor.of(
            Dctor.field("id", User::getIdLong),
            Dctor.field("username", User::getName)
    );

    public UserCacheDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return """
                CREATE TABLE IF NOT EXISTS username_cache (
                    id bigint NOT NULL,
                    username character varying(32) NOT NULL,
                    PRIMARY KEY (id)
                );
                """;
    }

    public void replaceAll(List<User> users) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            Query.of("INSERT INTO username_cache VALUES (:id, :username) ON CONFLICT (id) DO UPDATE SET username = excluded.username")
                    .execute(Batch.of(users, DCTOR), c);
            c.commit();
            c.setAutoCommit(true);
        }
    }

    public void deleteNonexistentUsers(List<User> users) throws SQLException {
        Set<Long> cachedUsers = users.stream().map(ISnowflake::getIdLong).collect(Collectors.toSet());
        List<FakeUser> dbUsers = getAllCachedUsers()
                .stream()
                .filter(o -> !cachedUsers.contains(o.id()))
                .toList();
        if (dbUsers.isEmpty()) {
            return; // nothing to do
        }
        // second constructor parameter is null here as we don't need to assign it a value
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            Query.of("DELETE FROM username_cache WHERE id = :id")
                    .execute(Batch.of(dbUsers, Dctor.of(Dctor.field("id", FakeUser::id))), c);
            c.commit();
            c.setAutoCommit(true);
        }
    }

    public int getCachedUserCount() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT COUNT(*) FROM username_cache")
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }

    private List<FakeUser> getAllCachedUsers() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM username_cache")
                    .as((rs, conn) -> {
                        List<FakeUser> users = new ArrayList<>();
                        while (rs.next()) {
                            users.add(new FakeUser(rs.getLong(1), rs.getString(2)));
                        }
                        return users;
                    }, c);
        }
    }
}
