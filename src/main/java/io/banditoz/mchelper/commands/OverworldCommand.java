package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TwoDimensionalPoint;

public class OverworldCommand extends Command {
    @Override
    public String commandName() {
        return "!overworld";
    }

    @Override
    protected void onCommand() {
        TwoDimensionalPoint point1 = new TwoDimensionalPoint(commandArgs[1], commandArgs[2]);
        TwoDimensionalPoint overworld = point1.getOverworldCoordinates();
        sendReply(overworld.toIntegerString());
    }
}
