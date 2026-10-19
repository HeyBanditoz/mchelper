package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.MorseUtils;
import jakarta.inject.Singleton;

@Singleton
public class FromMorseCommand extends BoundCommand {
    @Override
    public String commandName() {
        return "frommorse";
    }

    @Override
    protected String description() {
        return "Converts morse to text. Use / for spaces between words.";
    }

    @Override
    protected Handler declare() {
        return Params.of().rest("morse", "Morse code to convert.", this::fromMorse);
    }

    private Status fromMorse(ICommandEvent ce, String morse) {
        ce.sendReply(MorseUtils.fromMorse(morse));
        return Status.SUCCESS;
    }
}
