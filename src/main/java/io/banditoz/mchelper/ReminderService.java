package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ReminderRunnable;
import io.banditoz.mchelper.utils.database.Reminder;
import io.banditoz.mchelper.utils.database.dao.RemindersDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderService {
    private final ScheduledExecutorService SERVICE;
    private final MCHelper MCHELPER;
    private final Logger LOGGER = LoggerFactory.getLogger(ReminderService.class);

    public ReminderService(MCHelper mcHelper, ScheduledExecutorService service) {
        this.MCHELPER = mcHelper;
        this.SERVICE = service;
        initialize();
    }

    /**
     * Schedules a new reminder, and adding it to the database.
     *
     * @param r The ReminderRunnable to schedule.
     * @return The id of the newly added reminder in the database.
     */
    public int schedule(ReminderRunnable r) throws SQLException {
        Duration duration = Duration.between(Instant.now(), r.getReminder().getRemindWhen().toInstant());
        r.getReminder().setId(new RemindersDaoImpl(MCHELPER.getDatabase()).schedule(r.getReminder()));
        SERVICE.schedule(r, duration.getSeconds(), TimeUnit.SECONDS);
        LOGGER.debug("Scheduling: " + r.getReminder().toString());
        return r.getReminder().getId();
    }

    /**
     * Grabs all "active" reminders (those which haven't been reminded or deleted) and schedules them.
     */
    private void initialize() {
        try {
            ArrayList<Reminder> reminders = new ArrayList<>(new RemindersDaoImpl(MCHELPER.getDatabase()).getAllActiveReminders());
            for (Reminder reminder : reminders) {
                scheduleWithoutCreation(new ReminderRunnable(reminder, MCHELPER));
            }
            LOGGER.info("Initialized reminders. We have " + reminders.size() + " active reminders.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize reminders.", e);
        }
    }

    /**
     * Add a new Reminder to be fired to the ScheduledExecutorService without adding it to the database
     *
     * @param r The ReminderRunnable containing the Reminder to add.
     */
    private void scheduleWithoutCreation(ReminderRunnable r) {
        Duration duration = Duration.between(Instant.now(), r.getReminder().getRemindWhen().toInstant());
        SERVICE.schedule(r, duration.getSeconds(), TimeUnit.SECONDS);
        LOGGER.debug("Scheduling without creation: " + r.getReminder().toString());
    }
}
