package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.CoordinatePoint;

public class EangleCommand extends Command {
    @Override
    public String commandName() {
        return "eangle";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<x1> <z1> <x2> <z2>")
                .withDescription("Returns the elytra flight angle and distance between two Minecraftian coordinate points.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        CoordinatePoint point1 = new CoordinatePoint(ce.getCommandArgs()[1], ce.getCommandArgs()[2]);
        CoordinatePoint point2 = new CoordinatePoint(ce.getCommandArgs()[3], ce.getCommandArgs()[4]);
        ce.sendReply("**Yaw:** " + String.format("%.1f", point1.getAngleBetweenTwoPoints(point2)) +
                " **Distance:** " + String.format("%.1f", point1.getDistance(point2)));
        return Status.SUCCESS;
    }
}
