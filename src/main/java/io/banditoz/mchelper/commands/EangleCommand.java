package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TwoDimensionalPoint;


public class EangleCommand extends Command {
    @Override
    public String commandName() {
        return "!eangle";
    }

    @Override
    protected void onCommand() {
        try {
            TwoDimensionalPoint point1 = new TwoDimensionalPoint(commandArgs[1], commandArgs[2]);
            TwoDimensionalPoint point2 = new TwoDimensionalPoint(commandArgs[3], commandArgs[4]);

            sendReply("**Yaw:** " + String.format("%.1f", point1.getAngleBetweenTwoPoints(point2)) +
                    " **Distance:** " + String.format("%.1f", point1.getDistance(point2)));
        } catch (Exception ex) {
            sendExceptionMessage(ex);
        }
    }
}
