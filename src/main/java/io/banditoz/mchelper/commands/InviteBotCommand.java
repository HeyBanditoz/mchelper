package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.Permission;

public class InviteBotCommand extends Command {
    @Override
    public String commandName() {
        return "invite";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Generates an invite link to invite the bot.")
                .withParameters(null);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ce.sendReply("<" + ce.getEvent().getJDA().getInviteUrl(
                Permission.MESSAGE_READ,
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_ATTACH_FILES) + ">");
        return Status.SUCCESS;
    }
}
