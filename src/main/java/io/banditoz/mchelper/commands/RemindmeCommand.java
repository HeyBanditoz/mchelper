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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@RequiresDatabase
public class RemindmeCommand extends Command {
    private final ReminderService reminderService;
    private final RemindersDao remindersDao;
    private final JDA jda;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter ISO_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final Pattern DISCORD_TIMESTAMP_PATTERN =
            Pattern.compile("<t:(\\d+)(?::[tTdDfFR])?>");

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
        return new Help(commandName(), false)
                .withDescription("Creates a reminder. It is accurate give or take a second.")
                .withParameters("<duration/date/time> <reminder>");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        Duration d = getDurationFromString(ce.getCommandArgs()[1]);

        Timestamp t = new Timestamp(
                Instant.now().toEpochMilli() + (d.getSeconds() * 1000)
        );

        Instant in = t.toInstant();

        Reminder r = new Reminder();
        r.setAuthorId(ce.getEvent().getAuthor().getIdLong());
        r.setChannelId(ce.getEvent().getChannel().getIdLong());
        r.setReminder(ce.getCommandArgsString().replaceFirst("\\S+\\s+", ""));
        r.setRemindWhen(t);
        r.setIsFromDm(!ce.getEvent().isFromGuild());

        int id = reminderService.schedule(
                new ReminderRunnable(r, remindersDao, jda)
        );

        ce.sendReply(
                "Reminder " + id +
                        " coming at you at " +
                        TimeFormat.DATE_TIME_LONG.format(in) +
                        " (" +
                        TimeFormat.RELATIVE.format(in) +
                        ")"
        );

        return Status.SUCCESS;
    }

    private Duration getDurationFromString(String s) {
        try {
            return Duration.parse("P" + s.toUpperCase());
        } catch (DateTimeParseException ignored) {
            // Not a duration. Try the date/time formats below.
        }


        //Discord timestamp:<t:1778616000>
        Matcher matcher = DISCORD_TIMESTAMP_PATTERN.matcher(s);

        if (matcher.matches()) {
            long epochSeconds = Long.parseLong(matcher.group(1));
            Instant target = Instant.ofEpochSecond(epochSeconds);

            return Duration.between(Instant.now(), target);
        }

        /*
         * Date only: 2026-09-11
         * Defaults to midnight in the system's local timezone.
         */
        try {
            LocalDate date = LocalDate.parse(s, DATE_FORMAT);
            Instant target = date
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();

            return Duration.between(Instant.now(), target);
        } catch (DateTimeParseException ignored) {
            // Try date + time.
        }

        /*
         * Date and time: 2026-09-11 17:30
         */
        try {
            LocalDateTime dateTime = LocalDateTime.parse(
                    s,
                    DATE_TIME_FORMAT
            );

            Instant target = dateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            return Duration.between(Instant.now(), target);
        } catch (DateTimeParseException ignored) {
            // Try ISO format.
        }

         //ISO-8601 style: 2026-09-11T17:30
        try {
            LocalDateTime dateTime = LocalDateTime.parse(
                    s,
                    ISO_DATE_TIME_FORMAT
            );

            Instant target = dateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            return Duration.between(Instant.now(), target);
        } catch (DateTimeParseException ignored) {
            throw new IllegalArgumentException(
                    "Invalid reminder time. Use a duration (e.g. 2h), " +
                            "YYYY-MM-DD, YYYY-MM-DD HH:MM, or a Discord timestamp."
            );
        }
    }
}
