package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.MorseUtils;
import jakarta.inject.Singleton;

@Singleton
public class ToMorseCommand extends BoundCommand {
    @Override
    public String commandName() {
        return "tomorse";
    }

    @Override
    protected String description() {
        return "Encodes a message to morse code.";
    }

    @Override
    protected Handler declare() {
        return Params.of().rest("text", "String to convert to morse code.", this::toMorse);
    }

    private Status toMorse(ICommandEvent ce, String text) {
        ce.sendReply(MorseUtils.toMorse(text));
        return Status.SUCCESS;
    }
}
