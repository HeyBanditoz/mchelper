package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.money.Task;
import io.banditoz.mchelper.utils.database.Database;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

public class TasksDaoImpl extends Dao implements TasksDao {
    public TasksDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return """
                CREATE TABLE IF NOT EXISTS tasks (
                    id bigint,
                    task_id smallint,
                    can_run_again timestamp with time zone,
                    UNIQUE (id, task_id)
                );
                """;
    }

    @Override
    public LocalDateTime getWhenCanExecute(long id, Task t) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
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
        try (Connection c = DATABASE.getConnection()) {
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
