package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TwoDimensionalPoint;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class NetherCommand extends Command {
    @Override
    public String commandName() {
        return "!nether";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, String[] commandArgs) {
        try {
            TwoDimensionalPoint point1 = new TwoDimensionalPoint(commandArgs[1], commandArgs[2]);
            TwoDimensionalPoint nether = point1.getNetherCoordinates();
            sendReply(e, nether.toIntegerString());
        } catch (Exception ex) {
            sendExceptionMessage(e, ex);
        }
    }
}
