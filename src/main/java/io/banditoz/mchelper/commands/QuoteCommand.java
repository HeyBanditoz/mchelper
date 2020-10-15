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

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            List<Optional<NamedQuote>> nq = new ArrayList<>();
            ArrayList<Page> pages = new ArrayList<>();
            if (args.getList("quoteAndAuthor") != null && args.getList("quoteAndAuthor").isEmpty()) {
                nq.add(dao.getRandomQuote(ce.getGuild()));
            }
            else {
                String s = args.getList("quoteAndAuthor").stream().map(Object::toString).collect(Collectors.joining(" "));
                nq = dao.getQuotesByMatch(s, ce.getGuild());
            }
            EmbedBuilder eb = new EmbedBuilder();
            for (Optional<NamedQuote> namedQuoteOptional : nq) {
                eb.clear();
                if (namedQuoteOptional.isPresent()) {
                    NamedQuote namedQuote = namedQuoteOptional.get();
                    eb.setColor(Color.green);
                    eb.setDescription("" + namedQuote.getQuote() + "\n\n*" + namedQuote.getQuoteAuthor() + " — " + namedQuote.getDateFormated() + "*");
                    if (args.get("include_author")) {
                        ce.getMCHelper().getJDA().retrieveUserById(namedQuote.getAuthorId()).queue(user -> {
                            eb.setFooter("Added by " + user.getName(), user.getAvatarUrl());
                        }, throwable -> {
                            eb.setFooter("Added by " + namedQuote.getAuthorId(),"https://discord.com/assets/28174a34e77bb5e5310ced9f95cb480b.png");
                        });
                    }
                }
                else {
                    eb.setDescription("No quote found.");
                    eb.setColor(Color.RED);
                    return Status.FAIL;
                }
                pages.add(new Page(PageType.EMBED, eb.build()));
            }
            if (pages.size() == 0) {
                eb.setDescription("No quote found.");
                eb.setColor(Color.RED);
                pages.add(new Page(PageType.EMBED, eb.build()));
            }
            if (pages.size() == 1) {
                ce.getEvent().getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue();
            } else {
                ce.getEvent().getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                    Pages.paginate(success, pages, 1, TimeUnit.MINUTES);
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
        parser.addArgument("quoteAndAuthor")
                .help("quote content and quote attribution to search by")
                .nargs("*");
        return parser;
    }
}
