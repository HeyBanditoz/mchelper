package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        long to = ce.getEvent().getMessage().getMentionedMembers().get(0).getIdLong();
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
