package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.MorseUtils;

public class FromMorseCommand extends Command {
    @Override
    public String commandName() {
        return "!frommorse";
    }

    @Override
    protected void onCommand() {
        sendReply(MorseUtils.fromMorse(commandArgsString));
    }
}
