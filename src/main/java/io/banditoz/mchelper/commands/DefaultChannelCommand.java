package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.utils.MiscUtil;

import static io.banditoz.mchelper.commands.logic.CommandPermissions.*;

public class DefaultChannelCommand extends Command {
    @Override
    public String commandName() {
        return "!defaultchannel";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("(channel ID)")
                .withDescription("Gets or sets the default channel for this guild.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        if (ce.getCommandArgs().length == 1) {
            ce.sendReply(Database.getInstance().getGuildDataById(ce.getGuild()).getDefaultChannel() + " is the default channel.");
        }
        else if (isBotOwner(ce.getEvent().getAuthor()) || isGuildOwner(ce.getEvent().getAuthor(), ce.getGuild())) {
            MiscUtil.parseSnowflake(ce.getCommandArgs()[1]); // check if it is a valid discord snowflake (I think)
            Database.getInstance().getGuildDataById(ce.getGuild()).setDefaultChannel(ce.getCommandArgs()[1]);
            Database.getInstance().saveDatabase();
            ce.sendReply(ce.getCommandArgs()[1] + " is now the default channel.");
        }
        else {
            ce.sendReply("You are not the guild owner.");
        }
    }
}
