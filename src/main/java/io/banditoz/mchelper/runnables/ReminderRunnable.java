package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.database.Reminder;
import io.banditoz.mchelper.database.dao.RemindersDao;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReminderRunnable implements Runnable {
    private final Reminder reminder;
    private final RemindersDao dao;
    private final JDA jda;

    public ReminderRunnable(Reminder reminder, RemindersDao dao, JDA jda) {
        this.reminder = reminder;
        this.dao = dao;
        this.jda = jda;
    }

    public Reminder getReminder() {
        return reminder;
    }

    @Override
    public void run() {
        try {
            if (dao.isStillActiveOrNotDeleted(reminder.getId())) {
                if (!reminder.isFromDm()) {
                    TextChannel tc = jda.getTextChannelById(reminder.getChannelId());
                    tc.sendMessage(new MessageCreateBuilder().setContent(format(reminder)).setAllowedMentions(List.of(Message.MentionType.USER)).build()).queue();
                }
                else {
                    jda.retrieveUserById(reminder.getAuthorId()).complete().openPrivateChannel().complete().sendMessage(format(reminder)).queue();
                }
                dao.markReminded(reminder.getId());
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ReminderRunnable.class).error("Error while sending/marking the reminder.", e);
        }
    }

    private String format(Reminder r) {
        return CommandUtils.formatMessage("*Buzz* <@" + r.getAuthorId() + "> " + r.getReminder(), false);
    }
}
