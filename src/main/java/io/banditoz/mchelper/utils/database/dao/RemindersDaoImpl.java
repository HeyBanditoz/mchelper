package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.Reminder;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RemindersDaoImpl extends Dao implements RemindersDao {
    public RemindersDaoImpl(Database database) {
        super(database);
    }

    @Override
    public String getSqlTableGenerator() {
        return """
                CREATE TABLE IF NOT EXISTS reminders (
                    id serial,
                    channel_id bigint NOT NULL,
                    author_id bigint NOT NULL,
                    reminder character varying(1500) NOT NULL,
                    remind_when timestamp with time zone NOT NULL,
                    reminded boolean DEFAULT false NOT NULL,
                    is_dm boolean NOT NULL,
                    deleted boolean DEFAULT false NOT NULL,
                    PRIMARY KEY (id)
                );
                """;
    }

    @Override
    public void markReminded(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("UPDATE reminders SET reminded = true WHERE id=:i;")
                    .on(Param.value("i", id))
                    .executeUpdate(c);
        }
    }

    @Override
    public void markDeleted(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("UPDATE reminders SET deleted = true WHERE id=:i;")
                    .on(Param.value("i", id))
                    .executeUpdate(c);
        }
    }

    @Override
    public int schedule(Reminder r) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("INSERT INTO reminders (channel_id, author_id, reminder, remind_when, reminded, is_dm, deleted) VALUES (:c, :a, :r, :w, :e, :i, :d) RETURNING id")
                    .on(
                            Param.value("c", r.getChannelId()),
                            Param.value("a", r.getAuthorId()),
                            Param.value("r", r.getReminder()),
                            Param.value("w", r.getRemindWhen()),
                            Param.value("e", false),
                            Param.value("i", r.isFromDm()),
                            Param.value("d", false)
                    ).as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
        }
    }

    @Override
    public List<Reminder> getAllActiveReminders() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM reminders WHERE NOT (reminded OR deleted);")
                    .as((rs, conn) -> {
                        ArrayList<Reminder> reminders = new ArrayList<>();
                        while (rs.next()) {
                            reminders.add(buildReminderFromResultSet(rs));
                        }
                        return reminders;
                    }, c);
        }
    }

    @Override
    public boolean isStillActiveOrNotDeleted(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT NOT (deleted OR reminded) AS active FROM reminders WHERE id=:i;")
                    .on(Param.value("i", id))
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getBoolean("active");
                    }, c);
        }
    }

    @Override
    public Reminder getReminderById(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM reminders WHERE id=:i;")
                    .on(Param.value("i", id))
                    .as((rs, conn) -> {
                        rs.next();
                        return buildReminderFromResultSet(rs);
                    }, c);
        }
    }

    private Reminder buildReminderFromResultSet(ResultSet rs) throws SQLException {
        Reminder r = new Reminder();
        r.setId(rs.getInt("id"));
        r.setChannelId(rs.getLong("channel_id"));
        r.setAuthorId(rs.getLong("author_id"));
        r.setReminder(rs.getString("reminder"));
        r.setRemindWhen(rs.getTimestamp("remind_when"));
        r.setReminded(rs.getBoolean("reminded"));
        r.setIsFromDm(rs.getBoolean("is_dm"));
        r.setDeleted(rs.getBoolean("deleted"));
        return r;
    }
}
