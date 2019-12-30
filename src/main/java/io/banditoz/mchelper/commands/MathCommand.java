package io.banditoz.mchelper.commands;

import com.udojava.evalex.Expression;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;

import java.math.BigDecimal;

public class MathCommand extends Command {
    @Override
    public String commandName() {
        return "math";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<math>")
                .withDescription("Executes math. See <https://github.com/uklimaschewski/EvalEx#supported-operators> for what you can do.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        BigDecimal result;
        result = new Expression(ce.getCommandArgsString()).eval();
        if (result.toPlainString().length() >= 256) {
            ce.sendReply(result.toEngineeringString());
        }
        else {
            ce.sendReply(result.toPlainString());
        }
    }
}
