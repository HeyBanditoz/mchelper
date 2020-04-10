package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;

public class AddquoteCommand extends Command {
    @Override
    public String commandName() {
        return "addquote";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("\"<name>\" quote")
                .withDescription("Adds a quote to the database.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        try {
            NamedQuote nq = NamedQuote.parseString(ce.getCommandArgsString());
            nq.setGuildId(ce.getGuild().getIdLong());
            nq.setAuthorId(ce.getEvent().getAuthor().getIdLong());
            new QuotesDaoImpl().saveQuote(nq);
            ce.sendReply("Quote added.");
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
