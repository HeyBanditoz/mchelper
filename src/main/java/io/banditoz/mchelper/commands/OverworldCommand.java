package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.CoordinatePoint;

public class OverworldCommand extends Command {
    @Override
    public String commandName() {
        return "overworld";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<x1> <z1>")
                .withDescription("Returns the overworld coordinates given a Minecraftian point.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        CoordinatePoint point1 = new CoordinatePoint(ce.getCommandArgs()[1], ce.getCommandArgs()[2]);
        CoordinatePoint overworld = point1.getOverworldCoordinates();
        ce.sendReply(overworld.toIntegerString());
        return Status.SUCCESS;
    }
}
