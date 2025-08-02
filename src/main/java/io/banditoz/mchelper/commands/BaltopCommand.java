package io.banditoz.mchelper.commands;

import java.util.List;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.StatPoint;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;

@Singleton
@RequiresDatabase
public class BaltopCommand extends Command {
    private final AccountManager accountManager;

    @Inject
    public BaltopCommand(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

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
        List<StatPoint<String>> organized = accountManager.getTopBalancesForGuild(ce.getGuild());
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
