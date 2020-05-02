package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.SnowflakeUtils;

public class SnowflakeCommand extends Command {
    @Override
    public String commandName() {
        return "snowflake";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Converts Discord snowflake ID(s) to dates.")
                .withParameters("<snowflakes...>");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        ce.sendReply(SnowflakeUtils.returnDateTimesForIDs(ce.getCommandArgsString()));
    }
}
