package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;

import java.util.Random;

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
    protected void onCommand(CommandEvent ce) {
        if (new Random().nextBoolean()) {
            ce.sendReply("Heads!");
        }
        else {
            ce.sendReply("Tails!");
        }
    }
}
