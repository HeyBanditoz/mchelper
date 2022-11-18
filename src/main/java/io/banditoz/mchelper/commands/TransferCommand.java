package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Requires(database = true)
public class TransferCommand extends Command {
    @Override
    public String commandName() {
        return "transfer";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Transfer money.")
                .withParameters("<to> <amount> [memo]");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        AccountManager am = ce.getMCHelper().getAccountManager();
        //if account is blacklisted apply a 95% tax to transfers
        if (am.isUserShadowbanned(ce.getEvent().getAuthor())) {
            long to = ce.getMentionedMembers().get(0).getIdLong();
            BigDecimal amount = new BigDecimal(ce.getRawCommandArgs()[2]);
            BigDecimal deduction = amount.multiply(BigDecimal.valueOf(0.95));
            amount = amount.multiply(BigDecimal.valueOf(0.05));
            am.remove(deduction, ce.getEvent().getAuthor().getIdLong(), "transfer tax");
            BigDecimal remainingAmount = ce.getMCHelper().getAccountManager().transferTo(
                    amount,
                    ce.getEvent().getAuthor().getIdLong(),
                    to,
                    "transfer"); // placeholder
            ce.sendReply("After paying taxes on your transaction the remaining $" + AccountManager.format(amount.setScale(2, RoundingMode.HALF_UP))
                    + " to <@!" + to + "> was completed. You have $" + AccountManager.format(remainingAmount) + " left.");
            return Status.SUCCESS;
        }
        //if not blacklisted proceed as normal
        else {
            long to = ce.getMentionedMembers().get(0).getIdLong();
            BigDecimal amount = new BigDecimal(ce.getRawCommandArgs()[2]);
            BigDecimal remainingAmount = ce.getMCHelper().getAccountManager().transferTo(
                    amount,
                    ce.getEvent().getAuthor().getIdLong(),
                    to,
                    "transfer"); // placeholder
            ce.sendReply("Transfer of $" + AccountManager.format(amount.setScale(2, RoundingMode.HALF_UP))
                    + " to <@!" + to + "> complete. You have $" + AccountManager.format(remainingAmount) + " left.");
            return Status.SUCCESS;
        }
    }
}
