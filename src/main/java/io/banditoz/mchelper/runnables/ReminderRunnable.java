package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.utils.database.Reminder;
import io.banditoz.mchelper.utils.database.dao.RemindersDao;
import io.banditoz.mchelper.utils.database.dao.RemindersDaoImpl;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReminderRunnable implements Runnable {
    private final Reminder R;
    private final MCHelper MCHELPER;

    public ReminderRunnable(Reminder r, MCHelper mc) {
        this.R = r;
        this.MCHELPER = mc;
    }

    public Reminder getReminder() {
        return R;
    }

    @Override
    public void run() {
        try {
            RemindersDao dao = new RemindersDaoImpl(MCHELPER.getDatabase());
            if (dao.isStillActiveOrNotDeleted(R.getId())) {
                if (!R.isFromDm()) {
                    TextChannel tc = MCHELPER.getJDA().getTextChannelById(R.getChannelId());
                    tc.sendMessage(new MessageCreateBuilder().setContent(format(R)).setAllowedMentions(List.of(Message.MentionType.USER)).build()).queue();
                }
                else {
                    MCHELPER.getJDA().retrieveUserById(R.getAuthorId()).complete().openPrivateChannel().complete().sendMessage(format(R)).queue();
                }
                dao.markReminded(R.getId());
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ReminderRunnable.class).error("Error while sending/marking the reminder.", e);
        }
    }

    private String format(Reminder r) {
        return CommandUtils.formatMessage("*Buzz* <@" + r.getAuthorId() + "> " + r.getReminder(), false);
    }
}
