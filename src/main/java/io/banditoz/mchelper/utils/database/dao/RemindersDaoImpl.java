package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.Reminder;

import java.sql.*;
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
            PreparedStatement ps = c.prepareStatement("UPDATE reminders SET reminded = true WHERE id=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        }
    }

    @Override
    public void markDeleted(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("UPDATE reminders SET deleted = true WHERE id=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        }
    }

    @Override
    public int schedule(Reminder r) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO reminders (channel_id, author_id, reminder, remind_when, reminded, is_dm, deleted) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id");
//            ps.setNull(1, Types.INTEGER);
            ps.setLong(1, r.getChannelId());
            ps.setLong(2, r.getAuthorId());
            ps.setString(3, r.getReminder());
            ps.setTimestamp(4, r.getRemindWhen());
            ps.setBoolean(5, false);
            ps.setBoolean(6, r.isFromDm());
            ps.setBoolean(7, false);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int id = rs.getInt(1);
            ps.close();
            rs.close();
            return id;
        }
    }

    @Override
    public List<Reminder> getAllActiveReminders() throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            ArrayList<Reminder> reminders = new ArrayList<>();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM reminders WHERE NOT (reminded OR deleted)");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reminders.add(buildReminderFromResultSet(rs));
            }
            ps.close();
            rs.close();
            return reminders;
        }
    }

    @Override
    public boolean isStillActiveOrNotDeleted(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT NOT (deleted Or reminded) AS active FROM reminders WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            boolean active = rs.getBoolean("active");
            rs.close();
            return active;
        }
    }

    @Override
    public Reminder getReminderById(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM reminders WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            Reminder r = buildReminderFromResultSet(rs);
            rs.close();
            ps.close();
            return r;
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
