package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.RPGDice;

public class CoinFlipCommand extends Command {
    @Override
    public String commandName() {
        return "!flip";
    }

    @Override
    public void onCommand() {
        RPGDice r = new RPGDice(1, 2, 1, 0); // meh probably don't have to use
        String result = r.roll(); // RPGDice for this, but it has a static SecureRandom we can use.
        if (result.equals("1")) {
            sendReply("Heads!");
        }
        else if (result.equals("2")) {
            sendReply("Tails!");
        }
        else {
            throw new IllegalArgumentException("Something went wrong inside RPGDice.");
        }
    }
}