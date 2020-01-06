package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Database;

import static io.banditoz.mchelper.commands.logic.CommandPermissions.*;

public class PrefixCommand extends Command {
    @Override
    public String commandName() {
        return "prefix";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("(prefix)")
                .withDescription("Gets or sets the prefix for this guild. (By default, it is a '!' and must be a char.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        if (ce.getCommandArgs().length == 1) {
            ce.sendReply(Database.getInstance().getGuildDataById(ce.getGuild()).getPrefix() + " is this guild's prefix.");
        }
        else if (isBotOwner(ce.getEvent().getAuthor()) || isGuildOwner(ce.getEvent().getAuthor(), ce.getGuild())) {
            char desiredPrefix = ce.getCommandArgs()[1].charAt(0);
            Database.getInstance().getGuildDataById(ce.getGuild()).setPrefix(desiredPrefix);
            Database.getInstance().saveDatabase();
            ce.sendReply(ce.getCommandArgs()[1].charAt(0) + " is now this guild's prefix.");
        }
        else {
            ce.sendReply("You are not the guild owner.");
        }
    }
}
