package io.banditoz.mchelper.commands;

import java.io.ByteArrayOutputStream;
import java.util.List;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.plotter.TransactionHistoryPlotter;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.User;

@Singleton
@RequiresDatabase
public class BalanceGraphCommand extends Command {
    private final AccountsDao accountsDao;

    @Inject
    public BalanceGraphCommand(AccountsDao accountsDao) {
        this.accountsDao = accountsDao;
    }

    @Override
    public String commandName() {
        return "balgraph";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Graphs your transaction history, transaction" +
                " by transaction. Use -d to use dates instead").withParameters("[-d]");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        User u;
        if (ce.getMentionedUsers().isEmpty()) {
            u = ce.getEvent().getAuthor();
        }
        else {
            u = ce.getMentionedUsers().get(0);
        }
        int count = Integer.MAX_VALUE;
        if (ce.getCommandArgs().length > 1) {
            try {
                count = Integer.parseInt(ce.getCommandArgs()[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        List<Transaction> txns = accountsDao.getNTransactionsForUser(u.getIdLong(), count);
        if (txns.isEmpty()) {
            throw new MoneyException("There is no account history for " + u.getName());
        }

        TransactionHistoryPlotter thp = new TransactionHistoryPlotter(u.getName(), txns);
        ByteArrayOutputStream plot;
        if (ce.getCommandArgsString().contains("-d")) {
            plot = thp.plotWithDates();
        }
        else {
            plot = thp.plot();
        }
        ce.sendImageReply("Last " + txns.size() + " transactions for " + u.getName(), plot);
        return Status.SUCCESS;

    }
}
