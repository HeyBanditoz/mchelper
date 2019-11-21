package io.banditoz.mchelper.commands;

import com.udojava.evalex.Expression;
import io.banditoz.mchelper.utils.Help;

import java.math.BigDecimal;

public class MathCommand extends Command {
    @Override
    public String commandName() {
        return "!math";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<math>")
                .withDescription("Executes math. See <https://github.com/uklimaschewski/EvalEx#supported-operators> for what you can do.");
    }

    @Override
    protected void onCommand() {
        BigDecimal result;
        result = new Expression(commandArgsString).eval();
        if (result.toPlainString().length() >= 256) {
            sendReply(result.toEngineeringString());
        }
        else {
            sendReply(result.toPlainString());
        }
    }
}
