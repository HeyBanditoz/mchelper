package io.banditoz.mchelper.commands;

import java.util.concurrent.ThreadLocalRandom;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Singleton;

@Singleton
public class CoinFlipCommand extends BoundCommand {
    @Override
    public String commandName() {
        return "flip";
    }

    @Override
    protected String description() {
        return "Flips a coin.";
    }

    @Override
    protected Handler declare() {
        return Params.empty(this::flip);
    }

    private Status flip(ICommandEvent ce) {
        ce.sendReply(ThreadLocalRandom.current().nextBoolean() ? "Heads!" : "Tails!");
        return Status.SUCCESS;
    }
}
