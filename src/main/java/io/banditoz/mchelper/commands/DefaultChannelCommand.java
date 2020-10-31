package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.utils.MiscUtil;

import static io.banditoz.mchelper.commands.logic.CommandPermissions.isBotOwner;
import static io.banditoz.mchelper.commands.logic.CommandPermissions.isGuildOwner;

public class DefaultChannelCommand extends Command {
    @Override
    public String commandName() {
        return "defaultchannel";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("(channel ID)")
                .withDescription("Gets or sets the default channel for this guild.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        GuildConfigDao dao = new GuildConfigDaoImpl(ce.getDatabase());
        GuildConfig gc = dao.getConfig(ce.getGuild());
        if (ce.getCommandArgs().length == 1) {
            ce.sendReply(gc.getDefaultChannel() + " is the default channel.");
        }
        else if (isBotOwner(ce.getEvent().getAuthor(), ce.getMCHelper().getSettings()) || isGuildOwner(ce.getEvent().getAuthor(), ce.getGuild())) {
            gc.setDefaultChannel(MiscUtil.parseSnowflake(ce.getCommandArgs()[1]));
            dao.saveConfig(gc);
            ce.sendReply(ce.getCommandArgs()[1] + " is now the default channel.");
        }
        else {
            ce.sendReply("You are not the guild owner.");
        }
        return Status.SUCCESS;
    }
}
