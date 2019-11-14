package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.RPGDice;

public class DiceRollerCommand extends Command {
    @Override
    public String commandName() {
        return "!roll";
    }

    @Override
    protected void onCommand() {
        RPGDice r = RPGDice.parse(commandArgsString);
        sendReply(r.roll());
    }
}
