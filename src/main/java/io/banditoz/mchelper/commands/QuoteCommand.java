package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.NamedQuote;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildData;

import java.security.SecureRandom;
import java.util.*;

public class QuoteCommand extends Command {
    private SecureRandom random = new SecureRandom();

    @Override
    public String commandName() {
        return "quote";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[search]")
                .withDescription("Posts a random quote or optionally searches for one.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        ArrayList<NamedQuote> allQuotes = new ArrayList<>();
        for (GuildData guild : Database.getInstance().getAllGuildData()) {
            allQuotes.addAll(guild.getQuotes());
        }
        if (ce.getCommandArgs().length > 1) {
            ce.sendReply(fetchRandom(allQuotes, ce.getCommandArgsString()).buildResponse());
        }
        else {
            ce.sendReply(fetchRandom(allQuotes, "").buildResponse());
        }
    }

    private NamedQuote fetchRandom(List<NamedQuote> quotes, String search) {
        search = search.toLowerCase();
        if (!search.isEmpty()) {
            ArrayList<NamedQuote> matches = new ArrayList<>();
            for (NamedQuote quote : quotes) {
                if (quote.getQuote().toLowerCase().contains(search) || quote.getName().toLowerCase().contains(search)) {
                    matches.add(quote);
                }
            }
            return matches.get(random.nextInt(matches.size()));
        }
        else {
            return quotes.get(random.nextInt(quotes.size()));
        }
    }
}