package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.dao.TimeZoneInfoDao;
import io.banditoz.mchelper.utils.database.dao.TimeZoneInfoDaoImpl;

import java.util.TimeZone;

public class SetTimeZoneCommand extends Command {
    @Override
    public String commandName() {
        return "settz";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<TZ name>")
                .withDescription("Adds a timezone. See here for a list of supported ones: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        TimeZoneInfoDao dao = new TimeZoneInfoDaoImpl(ce.getDatabase());
        TimeZone userZone = TimeZone.getTimeZone(ce.getCommandArgsString());
        dao.insertOrReplaceTimeZone(ce.getEvent().getAuthor().getIdLong(), userZone);

        String reply = "Your timezone has been set to " + userZone.getDisplayName() + ".";
        if (userZone.getID().equals("GMT")) {
            reply += "\nNOTE: Your timezone is GMT. Java's behavior is to set all unknown timezones to GMT. Make sure your timezone is correct.";
        }
        ce.sendReply(reply);
        return Status.SUCCESS;
    }
}
