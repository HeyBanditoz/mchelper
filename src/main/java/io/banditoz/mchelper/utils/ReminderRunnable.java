package io.banditoz.mchelper.utils;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.utils.database.Reminder;
import io.banditoz.mchelper.utils.database.dao.RemindersDao;
import io.banditoz.mchelper.utils.database.dao.RemindersDaoImpl;
import org.slf4j.LoggerFactory;

public class ReminderRunnable implements Runnable {
    private final Reminder r;

    public ReminderRunnable(Reminder r) {
        this.r = r;
    }

    public Reminder getReminder() {
        return r;
    }

    @Override
    public void run() {
        try {
            RemindersDao dao = new RemindersDaoImpl();
            if (dao.isStillActiveOrNotDeleted(r.getId())) {
                MCHelper.getJDA().getTextChannelById(r.getChannelId()).sendMessage(
                        CommandUtils.formatMessage("*Buzz* <@" + r.getAuthorId() + "> " + r.getReminder())).queue();
                dao.markReminded(r.getId());
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ReminderRunnable.class).error("Error while sending/marking the reminder.", e);
        }
    }
}
