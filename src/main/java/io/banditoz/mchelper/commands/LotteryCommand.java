package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.lottery.LotteryManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Lottery;
import io.banditoz.mchelper.utils.database.LotteryEntrant;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.math.BigDecimal;
import java.util.List;

import static io.banditoz.mchelper.money.AccountManager.format;

@Requires(database = true)
public class LotteryCommand extends Command {
    @Override
    public String commandName() {
        return "lottery";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false)
                .withParameters("[amount]")
                .withDescription("Enter the lottery! They last for four hours.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        // TODO reduce the massive amounts of copypaste in here
        LotteryManager lm = ce.getMCHelper().getLotteryManager();
        Lottery l = lm.getLottery(ce.getGuild());
        if (ce.getCommandArgsString().isBlank()) {
            if (l != null) {
                List<LotteryEntrant> entrantsForLottery = lm.getEntrantsForLottery(ce.getGuild());
                BigDecimal sum = entrantsForLottery.stream()
                        .map(LotteryEntrant::amount)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                ce.sendReply("The guild currently has a lottery you can enter. The ticket limit is set at $" + format(l.limit()) + " with a pot of $" + format(sum) + ".\n" + getParticipantAndAmountsForLottery(entrantsForLottery));
            }
            else {
                BigDecimal limit = lm.getTicketLimitForGuild(ce.getGuild());
                ce.sendReply("The guild currently has no lottery. You may start one by rerunning this command and specifying an amount. The ticket limit would be $" + format(limit) + ".");
            }
        }
        else {
            if (l == null) {
                lm.startLotteryForGuild(ce.getEvent().getChannel().asTextChannel());
                l = lm.getLottery(ce.getGuild());
            }
            lm.enterMember(new BigDecimal(ce.getCommandArgsString()), ce.getEvent().getMember());
            List<LotteryEntrant> entrantsForLottery = lm.getEntrantsForLottery(ce.getGuild());
            BigDecimal sum = entrantsForLottery.stream()
                    .map(LotteryEntrant::amount)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            ce.sendReply("You have entered the lottery. It is set to end %s with a current pot of $%s. Entrant list:\n%s"
                    .formatted(TimeFormat.RELATIVE.format(l.drawAt().toInstant()), format(sum), getParticipantAndAmountsForLottery(entrantsForLottery)));
        }
        return Status.SUCCESS;
    }

    private String getParticipantAndAmountsForLottery(List<LotteryEntrant> entrantsForLottery) {
        StringBuilder participants = new StringBuilder();
        // build out total
        double sum = entrantsForLottery.stream()
                .map(LotteryEntrant::amount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        // summon someone using some runes
        for (LotteryEntrant entrant : entrantsForLottery) {
            participants.append("<@!")
                    .append(entrant.userId())
                    .append(">")
                    .append(": $")
                    .append(AccountManager.format(entrant.amount()))
                    .append(", ")
                    .append(String.format("%.2f", entrant.amount().doubleValue() / sum * 100))
                    .append("%.\n");
        }
        return participants.toString();
    }
}
