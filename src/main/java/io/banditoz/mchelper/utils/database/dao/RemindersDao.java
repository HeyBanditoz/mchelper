package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Reminder;

import java.sql.SQLException;
import java.util.List;

public interface RemindersDao {
    /**
     * Mark a reminder as reminded.
     *
     * @param id The ID to mark as reminded.
     * @throws SQLException If there was an error marking the reminder as reminded.
     */
    void markReminded(int id) throws SQLException;
    /**
     * Mark a reminder as deleted. A user does not want to be reminded of this anymore
     *
     * @param id The ID to mark as deleted.
     * @throws SQLException If there was an error marking the reminder as deleted.
     */
    void markDeleted(int id) throws SQLException;
    /**
     * Schedules a new reminder in the database. It should be added to a service that will remind the user at the right
     * time, and added to the database so it will persist through restarts.
     *
     * @param r The reminder to add.
     * @return The ID of the scheduled reminder.
     * @throws SQLException If there was an error scheduling the reminder in the database.
     */
    int schedule(Reminder r) throws SQLException;
    /**
     * Gets all reminders that aren't marked reminded or marked deleted.
     *
     * @return A {@link List} of all reminders that meet the above requirements, empty if there aren't any.
     * @throws SQLException If there was an error fetching the reminders.
     */
    List<Reminder> getAllActiveReminders() throws SQLException;
    /**
     * Checks if a reminder is still active in the database by its ID.
     *
     * @param id The ID to check.
     * @return uWhether or not the reminded is still active or hasn't been marked as deleted.
     * @throws SQLException If the reminder doesn't exist.
     */
    boolean isStillActiveOrNotDeleted(int id) throws SQLException;
    /**
     * Retrieves a reminder by an ID.
     *
     * @param id The ID to check.
     * @return The Reminder to get.
     * @throws SQLException If there was an error fetching the reminder.
     */
    Reminder getReminderById(int id) throws SQLException;
}
