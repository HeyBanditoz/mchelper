package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.RPGDice;

public class CoinFlipCommand extends Command {
    @Override
    public String commandName() {
        return "!flip";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Flips a coin.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        RPGDice r = new RPGDice(1, 2, 1, 0); // meh probably don't have to use
        String result = r.roll(); // RPGDice for this, but it has a static SecureRandom we can use.
        if (result.equals("1")) {
            ce.sendReply("Heads!");
        }
        else if (result.equals("2")) {
            ce.sendReply("Tails!");
        }
        else {
            throw new IllegalArgumentException("Something went wrong inside RPGDice.");
        }
    }
}
