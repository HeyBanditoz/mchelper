package io.banditoz.mchelper.commands;

import java.util.Collections;
import java.util.List;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Singleton
@RequiresDatabase
public class TransactionsCommand extends Command {
    private final AccountsDao dao;
    private final JDA jda;

    @Inject
    public TransactionsCommand(AccountsDao dao, JDA jda) {
        this.dao = dao;
        this.jda = jda;
    }

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
        List<MessageEmbed> pages = dao.getNTransactionsForUser(ce.getEvent().getAuthor().getIdLong(), 20).stream()
                .sorted(Collections.reverseOrder())
                .map(transaction -> transaction.render(jda))
                .toList();
        ce.sendEmbedPaginatedReplyWithPageNumber(pages);
        return Status.SUCCESS;
    }
}
