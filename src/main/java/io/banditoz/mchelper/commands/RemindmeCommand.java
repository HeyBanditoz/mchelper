package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.ReminderService;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.runnables.ReminderRunnable;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.database.Reminder;
import io.banditoz.mchelper.database.dao.RemindersDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Singleton
@RequiresDatabase
public class RemindmeCommand extends Command {
    private final ReminderService reminderService;
    private final RemindersDao remindersDao;
    private final JDA jda;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    @Inject
    public RemindmeCommand(ReminderService reminderService,
                           RemindersDao remindersDao,
                           JDA jda) {
        this.reminderService = reminderService;
        this.remindersDao = remindersDao;
        this.jda = jda;
    }

    @Override
    public String commandName() {
        return "remindme";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Creates a reminder. It is accurate give or take a second.")
                .withParameters("<duration> <reminder>");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        Duration d = getDurationFromString(ce.getCommandArgs()[1]);
        Timestamp t = new Timestamp(Instant.now().toEpochMilli() + (d.getSeconds() * 1000));
        Instant in = t.toInstant();
        Reminder r = new Reminder();
        r.setAuthorId(ce.getEvent().getAuthor().getIdLong());
        r.setChannelId(ce.getEvent().getChannel().getIdLong());
        r.setReminder(ce.getCommandArgsString().replaceFirst("\\S+\\s+", ""));
        r.setRemindWhen(t);
        r.setIsFromDm(!ce.getEvent().isFromGuild());
        int id = reminderService.schedule(new ReminderRunnable(r, remindersDao, jda));
        ce.sendReply("Reminder " + id + " coming at you at " + TimeFormat.DATE_TIME_LONG.format(in) + " (" + TimeFormat.RELATIVE.format(in) + ")");
        return Status.SUCCESS;
    }

    private Duration getDurationFromString(String s) throws ParseException {
        Duration d;
        try {
            d = Duration.parse("P" + s.toUpperCase());
        } catch (DateTimeParseException e) {
            // maybe it's a formatted date, then.
            Date date = format.parse(s);
            d = Duration.between(Instant.now(), date.toInstant());
        }
        return d;
    }
}
