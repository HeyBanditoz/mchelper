package io.banditoz.mchelper.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.banditoz.mchelper.commands.logic.params.ParamTypes.DECIMAL;
import static io.banditoz.mchelper.commands.logic.params.ParamTypes.USER;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.User;

@Singleton
@RequiresDatabase
public class TransferCommand extends BoundCommand {
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
    protected String description() {
        return "Transfer money.";
    }

    @Override
    protected Handler declare() {
        return Params.of()
                .arg("to", "Who the recipient of your gift is.", USER)
                .arg("amount", "How much to send.", DECIMAL)
                .handle(this::transfer);
    }

    private Status transfer(ICommandEvent ce, User recipient, BigDecimal amount) throws Exception {
        long to = recipient.getIdLong();
        long from = ce.getUser().getIdLong();

        // if account is blacklisted apply a 95% tax to transfers
        if (accountManager.isUserShadowbanned(ce.getUser())) {
            BigDecimal deduction = amount.multiply(BigDecimal.valueOf(0.95));
            amount = amount.multiply(BigDecimal.valueOf(0.05));
            accountManager.remove(deduction, from, "transfer tax");
            BigDecimal remainingAmount = accountManager.transferTo(amount, from, to, "transfer"); // placeholder
            ce.sendReply("After paying taxes on your transaction the remaining $" + AccountManager.format(amount.setScale(2, RoundingMode.HALF_UP))
                    + " to <@!" + to + "> was completed. You have $" + AccountManager.format(remainingAmount) + " left.");
        }
        // if not blacklisted proceed as normal
        else {
            BigDecimal remainingAmount = accountManager.transferTo(amount, from, to, "transfer"); // placeholder
            ce.sendReply("Transfer of $" + AccountManager.format(amount.setScale(2, RoundingMode.HALF_UP))
                    + " to <@!" + to + "> complete. You have $" + AccountManager.format(remainingAmount) + " left.");
        }
        return Status.SUCCESS;
    }
}
