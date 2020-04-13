package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
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
            if (ce.getCommandArgsString().isEmpty()) {
                ce.sendReply(dao.getRandomQuote(ce.getGuild()).format());
            } else {
                ce.sendReply(dao.getRandomQuoteByMatch(ce.getCommandArgsString(), ce.getGuild()).format());
            }
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
