package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.plotter.TransactionHistoryPlotter;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

@Requires(database = true)
public class BalanceGraphCommand extends Command {
    @Override
    public String commandName() {
        return "balgraph";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Graphs your transaction history, transaction" +
                " by transaction.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        AccountsDao dao = new AccountsDaoImpl(ce.getDatabase());
        User u;
        if (ce.getEvent().getMessage().getMentionedUsers().isEmpty()) {
            u = ce.getEvent().getAuthor();
        }
        else {
            u = ce.getEvent().getMessage().getMentionedUsers().get(0);
        }
        int count = Integer.MAX_VALUE;
        if (ce.getCommandArgs().length > 1) {
            try {
                count = Integer.parseInt(ce.getCommandArgs()[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        List<Transaction> txns = dao.getNTransactionsForUser(u.getIdLong(), count);
        if (txns.isEmpty()) {
            throw new MoneyException("There is no account history for " + u.getAsTag());
        }

        TransactionHistoryPlotter thp = new TransactionHistoryPlotter(u.getName(), txns);
        ce.sendImageReply("Last " + txns.size() + " transactions for " + u.getName(), thp.plot());
        return Status.SUCCESS;

    }
}
