package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.MorseUtils;

public class ToMorseCommand extends Command {
    @Override
    public String commandName() {
        return "!tomorse";
    }

    @Override
    protected void onCommand() {
        sendReply(MorseUtils.toMorse(commandArgsString));
    }
}
