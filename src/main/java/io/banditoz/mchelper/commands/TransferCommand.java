package io.banditoz.mchelper.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.slash.Param;
import io.banditoz.mchelper.commands.logic.slash.Slash;
import io.banditoz.mchelper.commands.logic.slash.SlashCommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.User;

@Singleton
@RequiresDatabase
public class TransferCommand extends Command {
    private final AccountManager accountManager;

    @Inject
    public TransferCommand(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

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
        User to = ce.getMentionedMembers().get(0).getUser();
        BigDecimal amount = new BigDecimal(ce.getRawCommandArgs()[2]);
        transferMoney(ce, ce.getUser(), to, amount);
        return Status.SUCCESS;
    }

    @Slash
    public Status onSlashCommand(SlashCommandEvent sce,
                                 @Param(desc = "Who the recipient of your gift is.") User to,
                                 @Param(desc = "How much to send.") double amount) throws Exception {
        transferMoney(sce, sce.getEvent().getUser(), to, new BigDecimal(amount));
        return Status.SUCCESS;
    }


    private void transferMoney(ICommandEvent ce, User invoker, User recipient, BigDecimal amount) throws Exception {
        long to = recipient.getIdLong();
        // if account is blacklisted apply a 95% tax to transfers
        if (accountManager.isUserShadowbanned(ce.getUser())) {
            BigDecimal deduction = amount.multiply(BigDecimal.valueOf(0.95));
            amount = amount.multiply(BigDecimal.valueOf(0.05));
            accountManager.remove(deduction, invoker.getIdLong(), "transfer tax");
            BigDecimal remainingAmount = accountManager.transferTo(
                    amount,
                    invoker.getIdLong(),
                    to,
                    "transfer"); // placeholder
            ce.sendReply("After paying taxes on your transaction the remaining $" + AccountManager.format(amount.setScale(2, RoundingMode.HALF_UP))
                    + " to <@!" + to + "> was completed. You have $" + AccountManager.format(remainingAmount) + " left.");
        }
        // if not blacklisted proceed as normal
        else {
            BigDecimal remainingAmount = accountManager.transferTo(
                    amount,
                    invoker.getIdLong(),
                    to,
                    "transfer"); // placeholder
            ce.sendReply("Transfer of $" + AccountManager.format(amount.setScale(2, RoundingMode.HALF_UP))
                    + " to <@!" + to + "> complete. You have $" + AccountManager.format(remainingAmount) + " left.");
        }
    }
}
