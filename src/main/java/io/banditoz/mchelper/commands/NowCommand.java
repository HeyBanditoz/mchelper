package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.utils.TimeFormat;

@Singleton
public class NowCommand extends Command {
    @Override
    public String commandName() {
        return "now";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Returns the current time.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ce.sendReply(TimeFormat.DATE_TIME_LONG.format(System.currentTimeMillis()));
        return Status.SUCCESS;
    }
}
