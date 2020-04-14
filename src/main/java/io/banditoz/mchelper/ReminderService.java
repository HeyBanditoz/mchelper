package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ReminderRunnable;
import io.banditoz.mchelper.utils.database.Reminder;
import io.banditoz.mchelper.utils.database.dao.RemindersDaoImpl;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReminderService {
    private static final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);

    /**
     * Schedules a new reminder, and adding it to the database.
     * @param r The ReminderRunnable to schedule.
     * @return The id of the newly added reminder in the database.
     * @throws SQLException
     */
    public static int schedule(ReminderRunnable r) throws SQLException {
        Duration duration = Duration.between(Instant.now(), r.getReminder().getRemindWhen().toInstant());
        r.getReminder().setId(new RemindersDaoImpl().schedule(r.getReminder()));
        service.schedule(r, duration.getSeconds(), TimeUnit.SECONDS);
        return r.getReminder().getId();
    }

    /**
     * Grabs all "active" reminders (those which haven't been reminded or deleted) and schedules them.
     * @throws SQLException If something went wrong with fetching all active reminders.
     */
    public static void initialize()  {
        try {
            ArrayList<Reminder> reminders = new ArrayList<>(new RemindersDaoImpl().getAllActiveReminders());
            for (Reminder reminder : reminders) {
                scheduleWithoutCreation(new ReminderRunnable(reminder));
            }
            LoggerFactory.getLogger(ReminderService.class).info("Initialized reminders. We have " + reminders.size() + " active reminders.");
        } catch (SQLException e) {
            LoggerFactory.getLogger(ReminderService.class).error("Failed to initialize reminders.", e);
        }
    }

    /**
     * Add a new Reminder to be fired to the ScheduledExecutorService without adding it to the database
     * @param r The ReminderRunnable containing the Reminder to add.
     */
    private static void scheduleWithoutCreation(ReminderRunnable r) {
        Duration duration = Duration.between(Instant.now(), r.getReminder().getRemindWhen().toInstant());
        service.schedule(r, duration.getSeconds(), TimeUnit.SECONDS);
    }
}
