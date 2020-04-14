package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Reminder;

import java.sql.SQLException;
import java.util.List;

public interface RemindersDao {
    void markReminded(int id) throws SQLException;
    void markDeleted(int id) throws SQLException;
    int schedule(Reminder r) throws SQLException;
    List<Reminder> getAllActiveReminders() throws SQLException;
    boolean isStillActiveOrNotDeleted(int id) throws SQLException;
    Reminder getReminderById(int id) throws SQLException;
}
