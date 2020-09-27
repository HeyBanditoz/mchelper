package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.plotter.FunctionPlotter;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

public class PlotCommand extends Command {
    @Override
    public String commandName() {
        return "plot";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<expression>").withDescription("plots a function, use x");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        FunctionPlotter fp = new FunctionPlotter(ce.getCommandArgsString(), -10.0, 10.0, -10.0, 10.0, 0.025);
        ce.sendImageReply("Plot of " + fp.getExpression(), fp.plot());
        return Status.SUCCESS;
    }
}
