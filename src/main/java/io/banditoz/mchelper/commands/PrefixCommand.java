package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;

import static io.banditoz.mchelper.commands.logic.CommandPermissions.isBotOwner;
import static io.banditoz.mchelper.commands.logic.CommandPermissions.isGuildOwner;

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
    protected Status onCommand(CommandEvent ce) throws Exception {
        GuildConfigDao dao = new GuildConfigDaoImpl(ce.getDatabase());
        if (ce.getCommandArgs().length == 1) {
            ce.sendReply(dao.getConfig(ce.getGuild()).getPrefix() + " is this guild's prefix.");
        }
        else if (isBotOwner(ce.getEvent().getAuthor(), ce.getSettings()) || isGuildOwner(ce.getEvent().getAuthor(), ce.getGuild())) {
            char desiredPrefix = ce.getCommandArgs()[1].charAt(0);
            GuildConfig gc = dao.getConfig(ce.getGuild());
            gc.setPrefix(desiredPrefix);
            dao.saveConfig(gc);
            ce.sendReply(desiredPrefix + " is now this guild's prefix.");
        }
        else {
            ce.sendReply("You are not the guild owner.");
            return Status.FAIL;
        }
        return Status.SUCCESS;
    }
}
