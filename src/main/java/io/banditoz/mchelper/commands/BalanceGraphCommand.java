package io.banditoz.mchelper.commands;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.ParamTypes;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.plotter.TransactionHistoryPlotter;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.User;

@Singleton
@RequiresDatabase
public class BalanceGraphCommand extends BoundCommand {
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
    protected String description() {
        return "Graphs your transaction history, transaction by transaction. Pass yes for dates to use dates instead.";
    }

    @Override
    protected Handler declare() {
        return Params.of()
                .optional("limit", "Max number of transactions to graph, newest first.", ParamTypes.INTEGER)
                .optional("user", "User to retrieve balance for. Defaults to self.", ParamTypes.USER)
                .optional("dates", "Whether to plot by dates instead of transaction by transaction.", ParamTypes.BOOLEAN)
                .handle(this::graph);
    }

    private Status graph(ICommandEvent ce, Optional<Integer> limitOpt, Optional<User> user,
                         Optional<Boolean> dates) throws Exception {
        User who = user.orElse(ce.getUser());
        int limit = limitOpt.orElse(Integer.MAX_VALUE);
        boolean withDates = dates.orElse(false);

        List<Transaction> txns = accountsDao.getNTransactionsForUser(who.getIdLong(), limit);
        if (txns.isEmpty()) {
            throw new MoneyException("There is no account history for " + who.getName());
        }

        TransactionHistoryPlotter thp = new TransactionHistoryPlotter(who.getName(), txns);
        ByteArrayOutputStream plot = withDates ? thp.plotWithDates() : thp.plot();
        ce.sendImageReply("Last " + txns.size() + " transactions for " + who.getName(), plot);
        return Status.SUCCESS;
    }
}
