package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Collections;
import java.util.List;

@Requires(database = true)
public class TransactionsCommand extends Command {
    @Override
    public String commandName() {
        return "txns";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Fetches your last 20 transactions.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        AccountsDao dao = new AccountsDaoImpl(ce.getMCHelper().getDatabase());
        List<MessageEmbed> pages = dao.getNTransactionsForUser(ce.getEvent().getAuthor().getIdLong(), 20).stream()
                .sorted(Collections.reverseOrder())
                .map(transaction -> transaction.render(ce.getMCHelper().getJDA()))
                .toList();
        ce.sendEmbedPaginatedReplyWithPageNumber(pages);
        return Status.SUCCESS;
    }
}
