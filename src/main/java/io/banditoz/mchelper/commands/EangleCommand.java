package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.TwoDimensionalPoint;


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
    protected void onCommand(CommandEvent ce) {
        TwoDimensionalPoint point1 = new TwoDimensionalPoint(ce.getCommandArgs()[1], ce.getCommandArgs()[2]);
        TwoDimensionalPoint point2 = new TwoDimensionalPoint(ce.getCommandArgs()[3], ce.getCommandArgs()[4]);
        ce.sendReply("**Yaw:** " + String.format("%.1f", point1.getAngleBetweenTwoPoints(point2)) +
                " **Distance:** " + String.format("%.1f", point1.getDistance(point2)));
    }
}
