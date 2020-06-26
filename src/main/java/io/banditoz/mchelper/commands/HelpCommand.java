package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;

import java.util.*;

public class HelpCommand extends Command {
    private final TreeMap<String, Help> helps = new TreeMap<>(); // TreeMap keeps it sorted

    @Override
    public String commandName() {
        return "help";
    }

    public HelpCommand(List<Command> commands) {
        helps.put(this.commandName(), getHelp()); // let's add ourselves into the mix, eh?
        commands.forEach(c -> helps.put(c.commandName(), c.getHelp()));
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("This screen.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        if (!(ce.getCommandArgs().length > 1)) {
            char prefix = new GuildConfigDaoImpl(ce.getDatabase()).getConfig(ce.getGuild()).getPrefix();
            StringBuilder sb = new StringBuilder();
            for (String s : helps.keySet()) {
                sb.append('`').append(prefix).append(s).append("` ");
            }
            ce.sendReply(sb.toString());
        }
        else {
            if (helps.containsKey(ce.getCommandArgs()[1])) {
                ce.sendReply(helps.get(ce.getCommandArgs()[1]).toString());
            }
            else {
                ce.sendReply("That command does not exist.");
            }
        }
    }
}
