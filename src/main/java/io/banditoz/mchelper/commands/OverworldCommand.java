package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TwoDimensionalPoint;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class OverworldCommand extends Command {
    @Override
    public String commandName() {
        return "!overworld";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, String[] commandArgs) {
        try {
            TwoDimensionalPoint point1 = new TwoDimensionalPoint(commandArgs[1], commandArgs[2]);
            TwoDimensionalPoint overworld = point1.getOverworldCoordinates();
            sendReply(e, overworld.toIntegerString());
        } catch (Exception ex) {
            sendExceptionMessage(e, ex);
        }
    }
}
