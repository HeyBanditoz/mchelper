package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.RPGDice;

public class DiceRollerCommand extends Command {
    @Override
    public String commandName() {
        return "!roll";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("xdy")
                .withDescription("Rolls x number of dice with y faces.");
    }

    @Override
    protected void onCommand() {
        RPGDice r = RPGDice.parse(commandArgsString);
        sendReply(r.roll());
    }
}
