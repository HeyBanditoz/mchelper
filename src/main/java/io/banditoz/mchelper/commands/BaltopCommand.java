package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.StatPoint;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.banditoz.mchelper.utils.StringUtils.*;

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
        ce.sendEmbedReply(new EmbedBuilder()
                .setAuthor("Money leaderboard for " + ce.getGuild().getName(), null, ce.getGuild().getIconUrl())
                .appendDescription(generateBaltopTable(organized))
                .build());
        return Status.SUCCESS;
    }

    private String generateBaltopTable(List<StatPoint<String, BigDecimal>> stats) {
        StringBuilder sb = new StringBuilder("\n```Rank  Name\n");
        for (int i = 1; i < stats.size(); i++) {
            StatPoint<String, BigDecimal> point = stats.get(i - 1);
            String name = padZeros(truncate(point.getThing().replace("`", ""), 16, false), 20);

            sb.append(padZeros(String.valueOf(i) + '.', 5)).append(name);
            sb.append('$').append(AccountManager.format(point.getCount())).append('\n');
        }
        return sb.toString() + "```";
    }
}
