package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.CryptoTicker;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CryptoCommand extends Command {
    @Override
    public String commandName() {
        return "!crypto";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, String[] commandArgs) {
        try {
            CryptoTicker ct = new CryptoTicker(commandArgs[1]);
        } catch (Exception ex) {
            sendExceptionMessage(e, ex);
        }
    }
}
