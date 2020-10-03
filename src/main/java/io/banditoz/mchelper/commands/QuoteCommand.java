package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
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
    protected Status onCommand(CommandEvent ce) throws Exception {
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
            if (nq.isPresent()) {
                NamedQuote namedQuote = nq.get();
                if (args.get("include_author")) {
                    ce.getMCHelper().getJDA().retrieveUserById(namedQuote.getAuthorId()).queue(user -> {
                        ce.sendReply(namedQuote.format() + " (author is " + user.getAsMention() + ")");
                    }, throwable -> {
                        ce.sendReply(namedQuote.format() + " (author is " + namedQuote.getAuthorId() + ")");
                    });
                }
                else {
                    ce.sendReply(namedQuote.format());
                }
            }
            else {
                ce.sendReply("No quote found.");
                return Status.FAIL;
            }
        }
        return Status.SUCCESS;
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
        List<StatPoint<Long>> quotes = dao.getUniqueAuthorQuoteCountPerGuild(ce.getGuild());
        if (quotes.isEmpty()) {
            return "This guild has no quotes to gather statistics for.";
        }
        int quoteCount = quotes.stream().mapToInt(StatPoint::getCount).sum();
        StringBuffer reply = new StringBuffer("We have " + quoteCount + " quotes for this guild.\n```\n");
        quotes.forEach((us) -> {
            try {
                reply.append(ce.getMCHelper().getJDA().retrieveUserById(us.getThing()).complete().getAsTag()).append(": ").append(us.getCount()).append('\n');
            } catch (Exception ex) {
                reply.append(us.getThing()).append(": ").append(us.getCount()).append('\n');
            }
        });
        return reply.toString() + "```";
    }

    private ArgumentParser getDefaultArgs() {
        ArgumentParser parser = ArgumentParsers.newFor("quote").addHelp(false).build();
        parser.addArgument("-s", "--stats")
                .action(Arguments.storeTrue())
                .help("retrieve stats instead");
        parser.addArgument("-i", "--include-author")
                .action(Arguments.storeTrue())
                .help("include who added the quote");
        parser.addArgument("quoteAndAuthor")
                .help("quote content and quote attribution to search by")
                .nargs("*");
        return parser;
    }
}
