package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;

import java.util.StringJoiner;

@Requires(database = true)
public class TransactionsCommand extends Command {
    @Override
    public String commandName() {
        return "txns";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Fetches your last 10 transactions.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        AccountsDao dao = new AccountsDaoImpl(ce.getMCHelper().getDatabase());
        StringJoiner sj = new StringJoiner("\n");
        for (Transaction txn : dao.getNTransactionsForUser(ce.getEvent().getAuthor().getIdLong(), 10)) {
            sj.add(txn.toString());
        }
        ce.sendReply("Here are your last ten transactions:\n```\n" + sj + "\n```");
        return Status.SUCCESS;
    }
}
