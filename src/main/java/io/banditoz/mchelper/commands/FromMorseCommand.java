package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.MorseUtils;

public class FromMorseCommand extends Command {
    @Override
    public String commandName() {
        return "!frommorse";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<morse>")
                .withDescription("Converts morse to text. Use / for spaces between words.");
    }

    @Override
    protected void onCommand() {
        sendReply(MorseUtils.fromMorse(commandArgsString));
    }
}
