package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.TwoDimensionalPoint;

public class NetherCommand extends Command {
    @Override
    public String commandName() {
        return "!nether";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<x1> <z1>")
                .withDescription("Returns the nether coordinates given a Minecraftian point.");
    }

    @Override
    protected void onCommand() {
        TwoDimensionalPoint point1 = new TwoDimensionalPoint(commandArgs[1], commandArgs[2]);
        TwoDimensionalPoint nether = point1.getNetherCoordinates();
        sendReply(nether.toIntegerString());
    }
}
