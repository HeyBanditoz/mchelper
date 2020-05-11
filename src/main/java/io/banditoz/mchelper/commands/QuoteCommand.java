package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;

public class QuoteCommand extends Command {
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
        QuotesDao dao = new QuotesDaoImpl();
        try {
            NamedQuote nq;
            if (ce.getCommandArgsString().isEmpty()) {
                nq = dao.getRandomQuote(ce.getGuild());
            }
            else {
                nq = dao.getRandomQuoteByMatch(ce.getCommandArgsString(), ce.getGuild());
            }
            ce.sendReply(nq == null ? "No quote found." : nq.format());
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
