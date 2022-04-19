package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.money.Task;
import io.banditoz.mchelper.utils.database.Database;

import java.sql.*;
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
            PreparedStatement ps = c.prepareStatement("SELECT can_run_again FROM tasks WHERE id = ? AND task_id = ?;");
            ps.setLong(1, id);
            ps.setInt(2, t.ordinal());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                rs.close();
                return LocalDateTime.now().minusSeconds(1); // hacky?
            }
            LocalDateTime ldt = rs.getTimestamp(1).toLocalDateTime();
            ps.close();
            rs.close();
            return ldt;
        }
    }

    @Override
    public void putOrUpdateTask(long id, Task t) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO tasks VALUES (?, ?, ?) ON CONFLICT (id, task_id) DO UPDATE SET can_run_again = excluded.can_run_again");
            ps.setLong(1, id);
            ps.setInt(2, t.ordinal());
            Timestamp time = Timestamp.from(Instant.now().plusSeconds(t.getDelay()));
            ps.setTimestamp(3, time);
            ps.execute();
            ps.close();
        }
    }
}
