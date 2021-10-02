package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.dao.StatisticsDao;
import io.banditoz.mchelper.utils.database.dao.StatisticsDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;

import java.text.DecimalFormat;
import java.util.List;

@Requires(database = true)
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
        List<StatPoint<String, Integer>> stats = dao.getUniqueCommandCountPerGuildOrGlobally(ce.getGuild());
        if (stats.isEmpty()) {
            ce.sendReply("No commands run for this guild, somehow.");
            return Status.FAIL;
        }
        ce.sendEmbedReply(new EmbedBuilder()
                .setAuthor("Command and listener statistics for " + ce.getGuild().getName(), null, ce.getGuild().getIconUrl())
                .appendDescription(generateStatsTable(stats))
                .build());
        return Status.SUCCESS;
    }

    private String generateStatsTable(List<StatPoint<String, Integer>> list) {
        return StatPoint.statsToPrettyLeaderboard(list, 16,
                s -> s.replace("Command", "").replace("Regexable", ""),
                count -> DecimalFormat.getInstance().format(count));
    }
}
