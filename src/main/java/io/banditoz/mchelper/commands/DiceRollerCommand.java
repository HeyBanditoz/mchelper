package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.github.tfriedrichs.dicebot.expression.DiceExpression;
import io.github.tfriedrichs.dicebot.result.DiceResultPrettyPrinter;

public class DiceRollerCommand extends Command {
    @Override
    public String commandName() {
        return "roll";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("xdy[k[l|h]]...")
                .withDescription("Rolls x number of dice with y faces. Supports keep-highs or keep-lows.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        DiceExpression e = DiceExpression.parse(ce.getCommandArgsString());
        ce.sendReply(new DiceResultPrettyPrinter().prettyPrint(e.roll()));
    }
}
