package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.StatPoint;
import net.dv8tion.jda.api.entities.Member;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LeaderboardCommand extends Command {
    @Override
    public String commandName() {
        return "leaderboard";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Gets the money leaderboard for this guild.")
                .withParameters(null);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        List<StatPoint<String, BigDecimal>> organized = ce.getMCHelper().getAccountManager().getAllBalances().stream()
                .map(point -> {
                    Member m = ce.getGuild().getMemberById(point.getThing());
                    if (m == null) {
                        return null;
                    }
                    else {
                        return new StatPoint<>(m.getEffectiveName(), point.getCount());
                    }
                })
                .filter(Objects::nonNull)
                .sorted(StatPoint::compareTo)
                .collect(Collectors.toList());
        StringBuilder reply = new StringBuilder("Monetary leaderboard for this guild:\n```\n");
        for (StatPoint<String, BigDecimal> point : organized) {
            reply.append(point.getThing()).append(": ").append('$').append(AccountManager.format(point.getCount())).append('\n');
        }
        ce.sendReply(reply.toString() + "```");
        return Status.SUCCESS;
    }
}
