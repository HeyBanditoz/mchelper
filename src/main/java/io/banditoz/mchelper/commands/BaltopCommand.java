package io.banditoz.mchelper.commands;

import java.util.List;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.database.StatPoint;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;

@Singleton
@RequiresDatabase
public class BaltopCommand extends BoundCommand {
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
    protected String description() {
        return "Gets the money leaderboard for this guild.";
    }

    @Override
    protected Handler declare() {
        return Params.empty(this::baltop);
    }

    private Status baltop(ICommandEvent ce) throws Exception {
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
