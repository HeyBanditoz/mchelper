package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.MorseUtils;

public class ToMorseCommand extends Command {
    @Override
    public String commandName() {
        return "tomorse";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<string>")
                .withDescription("Encodes a message to morse code.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ce.sendReply(MorseUtils.toMorse(ce.getCommandArgsString()));
        return Status.SUCCESS;
    }
}
