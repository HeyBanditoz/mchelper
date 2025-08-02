package io.banditoz.mchelper;

import io.avaje.inject.PostConstruct;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.runnables.ReminderRunnable;
import io.banditoz.mchelper.database.Reminder;
import io.banditoz.mchelper.database.dao.RemindersDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiresDatabase
public class ReminderService {
    private final ScheduledExecutorService ses;
    private final RemindersDao remindersDao;
    private final JDA jda;
    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    @Inject
    public ReminderService(RemindersDao remindersDao,
                           ScheduledExecutorService service,
                           JDA jda) {
        this.remindersDao = remindersDao;
        this.ses = service;
        this.jda = jda;
    }

    /**
     * Schedules a new reminder, and adding it to the database.
     *
     * @param r The ReminderRunnable to schedule.
     * @return The id of the newly added reminder in the database.
     */
    public int schedule(ReminderRunnable r) throws SQLException {
        Duration duration = Duration.between(Instant.now(), r.getReminder().getRemindWhen().toInstant());
        r.getReminder().setId(remindersDao.schedule(r.getReminder()));
        ses.schedule(r, duration.getSeconds(), TimeUnit.SECONDS);
        log.debug("Scheduling: " + r.getReminder().toString());
        return r.getReminder().getId();
    }

    /**
     * Grabs all "active" reminders (those which haven't been reminded or deleted) and schedules them.
     */
    @PostConstruct
    public void initialize() {
        try {
            ArrayList<Reminder> reminders = new ArrayList<>(remindersDao.getAllActiveReminders());
            for (Reminder reminder : reminders) {
                scheduleWithoutCreation(new ReminderRunnable(reminder, remindersDao, jda));
            }
            log.info("Initialized reminders. We have " + reminders.size() + " active reminders.");
        } catch (SQLException e) {
            log.error("Failed to initialize reminders.", e);
        }
    }

    /**
     * Add a new Reminder to be fired to the ScheduledExecutorService without adding it to the database
     *
     * @param r The ReminderRunnable containing the Reminder to add.
     */
    private void scheduleWithoutCreation(ReminderRunnable r) {
        Duration duration = Duration.between(Instant.now(), r.getReminder().getRemindWhen().toInstant());
        ses.schedule(r, duration.getSeconds(), TimeUnit.SECONDS);
        log.debug("Scheduling without creation: " + r.getReminder().toString());
    }
}
