package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

public class LoadoutCommand extends Command {
    @Override
    public String commandName() {
        return "loadout";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false)
                .withDescription("Points to ultimate-bravery.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ce.sendReply("Use https://www.ultimate-bravery.net/ instead.");
        return Status.SUCCESS;
    }
}
