package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.TeXRenderer;

public class TeXCommand extends Command {
    @Override
    public String commandName() {
        return "tex";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<tex>")
                .withDescription("Generates a mathematical equation using TeX markup.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        TeXRenderer.sendTeXToChannel(ce.getEvent(), ce.getCommandArgsString());
        return Status.SUCCESS;
    }
}
