package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.dao.TimeZoneInfoDao;
import io.banditoz.mchelper.utils.database.dao.TimeZoneInfoDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Requires(database = true)
public class TimeZoneCommand extends Command {
    @Override
    public String commandName() {
        return "time";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[user]")
                .withDescription("Gets a timezone for a user.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        TimeZoneInfoDao dao = new TimeZoneInfoDaoImpl(ce.getDatabase());
        List<User> mentionedUsers = ce.getEvent().getMessage().getMentionedUsers();
        User u;
        if (mentionedUsers.isEmpty()) {
            u = ce.getEvent().getAuthor();
        }
        else {
            u = mentionedUsers.get(0);
        }
        Optional<TimeZone> userZone = dao.getTimeZoneForId(u.getIdLong());

        if (userZone.isPresent()) {
            ZonedDateTime userTime = ZonedDateTime.now(userZone.get().toZoneId());
            ce.sendEmbedReply(new EmbedBuilder()
                    .setTitle("Time Information")
                    .setDescription(u.getAsMention() + "'s time is " + userTime.format(DateTimeFormatter.RFC_1123_DATE_TIME) + ".\n" +
                            "Their timezone is " + userZone.get().getDisplayName() + ".")
                    .setFooter("Clientside Local Time")
                    .setTimestamp(Instant.now())
                    .build());
            return Status.SUCCESS;
        }
        else {
            ce.sendEmbedReply(new EmbedBuilder()
                    .setTitle("Time Information")
                    .setDescription(u.getAsMention() + " does not have a timezone configured.")
                    .setColor(Color.RED)
                    .build());
            return Status.FAIL;
        }
    }
}
