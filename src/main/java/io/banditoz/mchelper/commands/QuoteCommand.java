package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.impl.action.StoreArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.Namespace;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class QuoteCommand extends Command {
    @Override
    public String commandName() {
        return "quote";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParser(getDefaultArgs());
    }

    @Override
    protected void onCommand(CommandEvent ce) throws Exception {
        Namespace args = getDefaultArgs().parseArgs(ce.getCommandArgsWithoutName());
        QuotesDao dao = new QuotesDaoImpl(ce.getDatabase());
        if (args.get("stats") != null && args.getBoolean("stats")) {
            ce.sendReply(getStatsString(ce, dao));
        }
        else {
            Optional<NamedQuote> nq;
            if (args.getList("quoteAndAuthor") != null && args.getList("quoteAndAuthor").isEmpty()) {
                nq = dao.getRandomQuote(ce.getGuild());
            }
            else {
                String s = args.getList("quoteAndAuthor").stream().map(Object::toString).collect(Collectors.joining(" "));
                nq = dao.getRandomQuoteByMatch(s, ce.getGuild());
            }
            nq.ifPresentOrElse(namedQuote -> ce.sendReply(namedQuote.format()), () -> ce.sendReply("No quote found."));
        }
    }

    /**
     * Formats a String that contains quote statistics for the given {@link net.dv8tion.jda.api.entities.Guild} in the
     * {@link CommandEvent}.
     *
     * @param ce  The CommandEvent to work off of.
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

    private ArgumentParser getDefaultArgs() {
        ArgumentParser parser = ArgumentParsers.newFor("quote").addHelp(false).build();
        parser.addArgument("-s", "--stats")
                .action(Arguments.storeTrue())
                .help("retrieve stats instead");
        parser.addArgument("quoteAndAuthor")
                .help("quote content and quote attribution to search by")
                .nargs("*");
        return parser;
    }
}
