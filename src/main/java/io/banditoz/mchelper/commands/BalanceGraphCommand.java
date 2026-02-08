package io.banditoz.mchelper.commands;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.List;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.slash.Param;
import io.banditoz.mchelper.commands.logic.slash.Slash;
import io.banditoz.mchelper.commands.logic.slash.SlashCommandEvent;
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
        sendHistory(ce, u, count, ce.getCommandArgsString().contains("-d"));
        return Status.SUCCESS;
    }

    @Slash
    public Status onSlashCommand(SlashCommandEvent sce,
                          @Nullable @Param(desc = "User to retrieve balance for. Defaults to self.") User user,
                          @Nullable @Param(desc = "Max number of transactions to graph. If you choose 10, it will retrieve the newest 10.") Integer limit,
                          @Nullable @Param(desc = "Whether to plot by dates. Put anything here to use this.") String plotUsingDates)
            throws Exception {
        sendHistory(sce, user == null ? sce.getUser() : user, limit == null ? Integer.MAX_VALUE : limit, plotUsingDates != null && !plotUsingDates.isBlank());
        return Status.SUCCESS;
    }

    private void sendHistory(ICommandEvent ce, User who, int limit, boolean withDates) throws Exception {
        List<Transaction> txns = accountsDao.getNTransactionsForUser(who.getIdLong(), limit);
        if (txns.isEmpty()) {
            throw new MoneyException("There is no account history for " + who.getName());
        }

        TransactionHistoryPlotter thp = new TransactionHistoryPlotter(who.getName(), txns);
        ByteArrayOutputStream plot = withDates ? thp.plotWithDates() : thp.plot();
        ce.sendImageReply("Last " + txns.size() + " transactions for " + who.getName(), plot);
    }
}
