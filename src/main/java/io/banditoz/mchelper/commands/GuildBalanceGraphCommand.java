package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.plotter.TransactionHistoryPlotter;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.Type;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import net.dv8tion.jda.api.entities.Guild;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Requires(database = true)
public class GuildBalanceGraphCommand extends Command {
    @Override
    public String commandName() {
        return "gbalgraph";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Graphs this guild's transactions history, transaction" +
                " by transaction.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        Guild g = ce.getGuild();
        AccountsDao dao = new AccountsDaoImpl(ce.getDatabase());
        BigDecimal runningSum = BigDecimal.ZERO;
        List<Transaction> txns = dao.getAllTransactions();
        List<Transaction> newTxns = new ArrayList<>(txns.size());
        for (Transaction t : dao.getAllTransactions()) {
            if (t.type() == Type.TRANSFER) {
                continue;
            }
            if (!(in(t.toN(), g) || in(t.fromN(), g))) {
                continue;
            }
            // TODO reduce throwaway objects here, both in runningSum reassignments and transactions below
            runningSum = runningSum.add(t.amount());
            newTxns.add(new Transaction(null, null, runningSum, BigDecimal.ZERO, null, t.date(), Type.GRANT));
        }
        TransactionHistoryPlotter thp = new TransactionHistoryPlotter(g.getName(), newTxns);
        ce.sendImageReply("Last " + newTxns.size() + " transactions for " + g.getName(), thp.plot());
        return Status.SUCCESS;
    }

    private boolean in(long userId, Guild g) {
        return g.getMemberById(userId) != null;
    }
}
