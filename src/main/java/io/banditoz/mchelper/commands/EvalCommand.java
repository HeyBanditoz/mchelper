package io.banditoz.mchelper.commands;

import com.udojava.evalex.Expression;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.math.BigDecimal;

public class EvalCommand extends Command {
    @Override
    public String commandName() {
        return "!math";
    }

    @Override
    protected void onCommand() {
        try {
            BigDecimal result;
            result = new Expression(commandArgsString).eval();
            if (result.toPlainString().length() >= 256) {
                sendReply(result.toEngineeringString());
            }
            else {
                sendReply(result.toPlainString());
            }
        } catch (Exception ex) {
            sendExceptionMessage(ex);
        }
    }
}
