package io.banditoz.mchelper.database.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.Task;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresDatabase
public class TasksDaoImpl extends Dao implements TasksDao {
    @Inject
    public TasksDaoImpl(Database database) {
        super(database);
    }

    @Override
    public LocalDateTime getWhenCanExecute(long id, Task t) throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT can_run_again FROM tasks WHERE id=:i AND task_id=:t;")
                    .on(
                            Param.value("i", id),
                            Param.value("t", t.ordinal())
                    ).as((rs, conn) -> {
                        if (!rs.next()) return LocalDateTime.now().minusSeconds(1); // hacky?
                        return rs.getTimestamp(1).toLocalDateTime();
                    }, c);
        }
    }

    @Override
    public void putOrUpdateTask(long id, Task t) throws SQLException {
        try (Connection c = database.getConnection()) {
            Timestamp time = Timestamp.from(Instant.now().plusSeconds(t.getDelay()));
            Query.of("INSERT INTO tasks VALUES (:i, :t, :c) ON CONFLICT (id, task_id) DO UPDATE SET can_run_again = excluded.can_run_again")
                    .on(
                            Param.value("i", id),
                            Param.value("t", t.ordinal()),
                            Param.value("c", time)
                    ).executeUpdate(c);
        }
    }
}
