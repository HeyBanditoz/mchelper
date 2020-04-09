package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.CoordinatePoint;

public class NetherCommand extends Command {
    @Override
    public String commandName() {
        return "nether";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<x1> <z1>")
                .withDescription("Returns the nether coordinates given a Minecraftian point.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        CoordinatePoint point1 = new CoordinatePoint(ce.getCommandArgs()[1], ce.getCommandArgs()[2]);
        CoordinatePoint nether = point1.getNetherCoordinates();
        ce.sendReply(nether.toIntegerString());
    }
}
