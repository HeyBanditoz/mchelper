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
    protected void onCommand(MessageReceivedEvent e, String[] commandArgs) {
        try {
            BigDecimal result;
            StringBuilder args = new StringBuilder();
            for (int i = 1; i < commandArgs.length; i++) {
                args.append(commandArgs[i]).append(" ");
            }
            result = new Expression(args.toString()).eval();
            if (result.toPlainString().length() >= 256) {
                sendReply(e, result.toEngineeringString());
            }
            else {
                sendReply(e, result.toPlainString());
            }
        } catch (Exception ex) {
            sendExceptionMessage(e, ex);
        }
    }
}
