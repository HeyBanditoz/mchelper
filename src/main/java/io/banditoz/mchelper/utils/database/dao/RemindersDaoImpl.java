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
        return "CREATE TABLE IF NOT EXISTS `reminders`( `id` int(11) AUTO_INCREMENT PRIMARY KEY, `channel_id` bigint(18) NOT NULL, `author_id` bigint(18) NOT NULL, `reminder` varchar(1500) COLLATE utf8mb4_unicode_ci NOT NULL, `remind_when` datetime NOT NULL, `reminded` tinyint(1) NOT NULL DEFAULT 0, `is_dm` tinyint(1) NOT NULL, `deleted` tinyint(1) NOT NULL DEFAULT 0, PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    @Override
    public void markReminded(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("UPDATE `reminders` SET reminded = 1 WHERE id=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        }
    }

    @Override
    public void markDeleted(int id) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("UPDATE `reminders` SET deleted = 1 WHERE id=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        }
    }

    @Override
    public int schedule(Reminder r) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO `reminders` VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setNull(1, Types.INTEGER);
            ps.setLong(2, r.getChannelId());
            ps.setLong(3, r.getAuthorId());
            ps.setString(4, r.getReminder());
            ps.setTimestamp(5, r.getRemindWhen());
            ps.setInt(6, 0);
            ps.setInt(7, r.isFromDm() ? 1 : 0);
            ps.setInt(8, 0);
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
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
            PreparedStatement ps = c.prepareStatement("SELECT * FROM `reminders` WHERE !(reminded || deleted)");
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
            PreparedStatement ps = c.prepareStatement("SELECT !(deleted || reminded) AS active FROM `reminders` WHERE id=?");
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
            PreparedStatement ps = c.prepareStatement("SELECT * FROM `reminders` WHERE id=?");
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
