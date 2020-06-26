package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.ReminderRunnable;
import io.banditoz.mchelper.utils.database.Reminder;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class RemindmeCommand extends Command {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

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
    protected void onCommand(CommandEvent ce) {
        Duration d;
        try {
            d = getDurationFromString(ce.getCommandArgs()[1]);
        } catch (ParseException e) {
            ce.sendExceptionMessage(e);
            return;
        }
        Reminder r = new Reminder();
        r.setAuthorId(ce.getEvent().getAuthor().getIdLong());
        r.setChannelId(ce.getEvent().isFromGuild() ? ce.getEvent().getChannel().getIdLong() : ce.getEvent().getPrivateChannel().getIdLong());
        r.setReminder(ce.getCommandArgsString().replaceFirst("\\S+\\s+", ""));
        r.setRemindWhen(new Timestamp(Instant.now().toEpochMilli() + (d.getSeconds() * 1000)));
        r.setIsFromDm(!ce.getEvent().isFromGuild());
        try {
            int id = ce.getMCHelper().getReminderService().schedule(new ReminderRunnable(r, ce.getMCHelper()));
            ce.sendReply("Reminder " + id + " coming at you in duration " + d + ".");
        } catch (SQLException e) {
            ce.sendExceptionMessage(e);
        }
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
