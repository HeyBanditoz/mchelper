package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;

import java.security.SecureRandom;
import java.util.ArrayList;

public class QuoteCommand extends Command {
    private SecureRandom random = new SecureRandom();

    @Override
    public String commandName() {
        return "quote";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[search]")
                .withDescription("Finds a random quote from this guild or optionally searches for one.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        ArrayList<NamedQuote> quotes;
        QuotesDao dao = new QuotesDaoImpl();
        try {
            if (ce.getCommandArgsString().isEmpty()) {
                quotes = new ArrayList<>(dao.getQuotes(ce.getGuild()));
            } else {
                quotes = new ArrayList<>(dao.getQuotesByMatch(ce.getCommandArgsString(), ce.getGuild()));
            }
            ce.sendReply(quotes.get(random.nextInt(quotes.size())).format());
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
