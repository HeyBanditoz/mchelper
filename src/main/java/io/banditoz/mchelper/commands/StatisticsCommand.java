package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.dao.StatisticsDao;
import io.banditoz.mchelper.utils.database.dao.StatisticsDaoImpl;

import java.util.Set;

public class StatisticsCommand extends Command {
    @Override
    public String commandName() {
        return "stats";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Returns command and other statistics.")
                .withParameters(null);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        StatisticsDao dao = new StatisticsDaoImpl(ce.getDatabase());
        Set<StatPoint<String>> stats = dao.getUniqueCommandCountPerGuildOrGlobally(ce.getGuild());
        if (stats.isEmpty()) {
            ce.sendReply("No commands run for this guild, somehow.");
            return Status.FAIL;
        }
        StringBuilder reply = new StringBuilder("Statistics for this guild:\n```\n");
        for (StatPoint<String> stat : stats) {
            reply.append(stat.toString()).append('\n');
        }
        ce.sendReply(reply.toString() + "```");
        return Status.SUCCESS;
    }
}
