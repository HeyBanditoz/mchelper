package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.StatPoint;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

@Requires(database = true)
public class BaltopCommand extends Command {
    @Override
    public String commandName() {
        return "baltop";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Gets the money leaderboard for this guild.")
                .withParameters(null);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        List<StatPoint<String>> organized = ce.getMCHelper().getAccountManager().getTopBalancesForGuild(ce.getGuild());
        ce.sendEmbedReply(new EmbedBuilder()
                .setAuthor("Money leaderboard for " + ce.getGuild().getName(), null, ce.getGuild().getIconUrl())
                .appendDescription(generateBaltopTable(organized))
                .build());
        return Status.SUCCESS;
    }

    private String generateBaltopTable(List<StatPoint<String>> stats) {
        return StatPoint.statsToPrettyLeaderboard(stats, 16,
                s -> s.replace("`", ""),
                bd -> "$" + AccountManager.format(bd));
    }
}
