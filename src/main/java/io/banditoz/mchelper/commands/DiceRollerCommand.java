package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.RPGDice;

public class DiceRollerCommand extends Command {
    @Override
    public String commandName() {
        return "roll";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("xdy")
                .withDescription("Rolls x number of dice with y faces.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        RPGDice r = RPGDice.parse(ce.getCommandArgsString());
        ce.sendReply(r.roll());
    }
}
