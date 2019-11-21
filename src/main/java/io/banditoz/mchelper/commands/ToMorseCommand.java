package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.MorseUtils;

public class ToMorseCommand extends Command {
    @Override
    public String commandName() {
        return "!tomorse";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<string>")
                .withDescription("Encodes a message to morse code.");
    }

    @Override
    protected void onCommand() {
        sendReply(MorseUtils.toMorse(commandArgsString));
    }
}
