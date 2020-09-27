package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

import java.util.concurrent.ThreadLocalRandom;

public class CoinFlipCommand extends Command {
    @Override
    public String commandName() {
        return "flip";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Flips a coin.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (ThreadLocalRandom.current().nextBoolean()) {
            ce.sendReply("Heads!");
        }
        else {
            ce.sendReply("Tails!");
        }
        return Status.SUCCESS;
    }
}
