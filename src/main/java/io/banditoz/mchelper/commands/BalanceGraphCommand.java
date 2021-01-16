package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.plotter.TransactionHistoryPlotter;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import net.dv8tion.jda.api.entities.User;

import java.util.Collections;
import java.util.List;

public class BalanceGraphCommand extends Command {
    @Override
    public String commandName() {
        return "balgraph";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Graphs your transaction history, transaction" +
                " by transaciton.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        AccountsDao dao = new AccountsDaoImpl(ce.getDatabase());
        User u = ce.getEvent().getAuthor();
        List<Transaction> txns = dao.getNTransactionsForUser(u.getIdLong(), Integer.MAX_VALUE);
        Collections.reverse(txns);

        TransactionHistoryPlotter thp = new TransactionHistoryPlotter(u.getName(), txns);
        ce.sendImageReply("Transaction history for " + u.getName(), thp.plot());
        return Status.SUCCESS;

    }
}
