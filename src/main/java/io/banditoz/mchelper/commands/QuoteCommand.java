package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        QuotesDao dao = new QuotesDaoImpl(ce.getDatabase());
        try {
            if (ce.getCommandArgs()[1].equals("stats")) {
                ce.sendReply(getStatsString(ce, dao));
            }
            else {
                Optional<NamedQuote> nq;
                if (ce.getCommandArgsString().isEmpty()) {
                    nq = dao.getRandomQuote(ce.getGuild());
                }
                else {
                    nq = dao.getRandomQuoteByMatch(ce.getCommandArgsString(), ce.getGuild());
                }
                nq.ifPresentOrElse(namedQuote -> ce.sendReply(namedQuote.format()), () -> ce.sendReply("No quote found."));
            }
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex);
        }
    }

    /**
     * Formats a String that contains quote statistics for the given {@link net.dv8tion.jda.api.entities.Guild} in the
     * {@link CommandEvent}.
     *
     * @param ce The CommandEvent to work off of.
     * @param dao The QuotesDao to gather stats from.
     * @return A formatted String for Discord that contains how many quotes an author has written.
     * @throws SQLException If there was an error with the database.
     */
    private String getStatsString(CommandEvent ce, QuotesDao dao) throws SQLException {
        Map<Long, Integer> quotes = dao.getUniqueAuthorQuoteCountPerGuild(ce.getGuild());
        if (quotes.isEmpty()) {
            return "This guild has no quotes to gather statistics for.";
        }
        int quoteCount = quotes.values().stream().mapToInt(integer -> integer).sum();
        AtomicReference<String> reply = new AtomicReference<>("We have " + quoteCount + " quotes for this guild.\n```\n");
        quotes.forEach((id, count) -> {
            String s = reply.get();
            try {
                s += ce.getMCHelper().getJDA().retrieveUserById(id).complete().getAsTag() + ": " + count + '\n';
            } catch (Exception ex) {
                s += id + ": " + count + '\n';
            }
            reply.set(s);
        });
        return reply.get() + "```";
    }
}
