package io.banditoz.mchelper.commands;

import com.udojava.evalex.Expression;

import java.math.BigDecimal;

public class EvalCommand extends Command {
    @Override
    public String commandName() {
        return "!math";
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
