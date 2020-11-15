package io.banditoz.mchelper.commands;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
            List<NamedQuote> quotes = new ArrayList<>();
            List<Page> pages = new ArrayList<>();

            if (args.getBoolean("all") != null && args.getBoolean("all")) {
                quotes = dao.getAllQuotesForGuild(ce.getGuild());
            }
            else if (args.getList("quoteAndAuthor") != null && args.getList("quoteAndAuthor").isEmpty()) {
                dao.getRandomQuote(ce.getGuild()).ifPresent(quotes::add);
            }
            else {
                String s = args.getList("quoteAndAuthor").stream().map(Object::toString).collect(Collectors.joining(" "));
                quotes = dao.getQuotesByMatch(s, ce.getGuild());
            }
            EmbedBuilder eb = new EmbedBuilder();
            for (int i = 0; i < quotes.size(); i++) {
                NamedQuote nq = quotes.get(i);
                eb.clear();
                eb.setColor(Color.GREEN);
                eb.setDescription(nq.format() + " *(" + (i + 1) + " of " + quotes.size() + ")*");
                if (args.get("include_author")) {
                    ce.getMCHelper().getJDA().retrieveUserById(nq.getAuthorId()).queue(user -> {
                        eb.setFooter("Added by " + user.getName(), user.getAvatarUrl());
                    }, throwable -> {
                        eb.setFooter("Added by " + nq.getAuthorId(), "https://discord.com/assets/28174a34e77bb5e5310ced9f95cb480b.png");
                    });
                }
                pages.add(new Page(PageType.EMBED, eb.build()));
            }
            if (pages.size() == 0) {
                eb.setDescription("No quote found.");
                eb.setColor(Color.RED);
                ce.sendEmbedReply(eb.build());
                return Status.FAIL;
            }
            if (pages.size() == 1) {
                ce.sendEmbedReply(eb.build());
            }
            else {
                ce.getEvent().getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                    Pages.paginate(success, pages, 1, TimeUnit.MINUTES, ce.getEvent().getAuthor()::equals);
                });

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
        parser.addArgument("-a", "--all")
                .action(Arguments.storeTrue())
                .help("get all quotes by a guild, skipping quoteAndAuthor");
        parser.addArgument("quoteAndAuthor")
                .help("quote content and quote attribution to search by")
                .nargs("*");
        return parser;
    }
}
