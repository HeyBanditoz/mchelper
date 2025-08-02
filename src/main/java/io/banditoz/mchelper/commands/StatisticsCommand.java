package io.banditoz.mchelper.commands;

import java.text.DecimalFormat;
import java.util.List;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.StatPoint;
import io.banditoz.mchelper.database.dao.StatisticsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;

@Singleton
@RequiresDatabase
public class StatisticsCommand extends Command {
    private final StatisticsDao dao;

    @Inject
    public StatisticsCommand(StatisticsDao dao) {
        this.dao = dao;
    }

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
        List<StatPoint<String>> stats = dao.getUniqueCommandCountPerGuildOrGlobally(ce.getGuild());
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

    private String generateStatsTable(List<StatPoint<String>> list) {
        return StatPoint.statsToPrettyLeaderboard(list, 16,
                s -> s.replace("Command", "").replace("Regexable", ""),
                count -> DecimalFormat.getInstance().format(count));
    }
}
