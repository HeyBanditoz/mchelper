package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.StatPoint;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.awt.Color;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Requires(database = true)
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
            ce.sendEmbedReply(new EmbedBuilder()
                    .setAuthor("Quote leaderboard for " + ce.getGuild().getName(), null, ce.getGuild().getIconUrl())
                    .appendDescription(getStatsString(ce, dao))
                    .build());
        }
        else {
            List<NamedQuote> quotes;
            List<MessageEmbed> embeds = new ArrayList<>();

            if (args.getBoolean("all") != null && args.getBoolean("all")) {
                quotes = dao.getAllQuotesForGuild(ce.getGuild());
            }
            else if (!ce.getMentionedUsers().isEmpty()) {
                quotes = dao.getAllQuotesByAuthorInGuild(ce.getMentionedUsers().get(0).getIdLong(), ce.getGuild());
            }
            else if (args.getList("quoteAndAuthor") != null && args.getList("quoteAndAuthor").isEmpty()) {
                NamedQuote nq = dao.getRandomQuote(ce.getGuild());
                if (nq == null) {
                    quotes = Collections.emptyList();
                }
                else {
                    quotes = Collections.singletonList(nq);
                }
            }
            else {
                String s = args.getList("quoteAndAuthor").stream().map(Object::toString).collect(Collectors.joining(" "));
                if (!(s.startsWith("`") && s.endsWith("`"))) {
                    quotes = dao.getQuotesByMatch(s, ce.getGuild());
                }
                else {
                    s = s.substring(1, s.length() - 1);
                    quotes = dao.getAllQuotesForGuild(ce.getGuild());
                    // do regex processing on java side as opposed to SQL, so user gets feedback if their regex is invalid
                    // it's probably extremely inefficient to do it here, but fight me
                    Pattern p = Pattern.compile(s);
                    quotes = quotes.stream().filter(namedQuote -> {
                        Matcher m = p.matcher(namedQuote.getQuote());
                        if (m.find()) {
                            return true;
                        }
                        else {
                            m = p.matcher(namedQuote.getQuoteAuthor());
                            return m.find();
                        }
                    }).toList();
                }
            }
            EmbedBuilder eb = new EmbedBuilder();
            for (int i = 0; i < quotes.size(); i++) {
                NamedQuote nq = quotes.get(i);
                eb.clear();
                eb.setColor(Color.GREEN);
                eb.setDescription(nq.format(args.get("id") != null && args.getBoolean("id")) + " *(" + (i + 1) + " of " + quotes.size() + ")*");
                if (args.get("include_author")) {
                    ce.getMCHelper().getJDA().retrieveUserById(nq.getAuthorId()).queue(
                            user -> eb.setFooter("Added by " + user.getName(), user.getAvatarUrl()),
                            throwable -> eb.setFooter("Added by " + nq.getAuthorId(), "https://discord.com/assets/28174a34e77bb5e5310ced9f95cb480b.png")
                    );
                }
                embeds.add(eb.build());
            }
            if (embeds.size() == 0) {
                eb.setDescription("No quote found.");
                eb.setColor(Color.RED);
                ce.sendEmbedReply(eb.build());
                return Status.FAIL;
            }
            else {
                ce.sendEmbedPaginatedReply(embeds);
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
        return StatPoint.statsToPrettyLeaderboard(quotes, 16,
                id -> tryGetEffectiveMemberNameById(id, ce.getGuild()).replace("`", ""),
                count -> DecimalFormat.getInstance().format(count));
    }

    /**
     * Tries to get a member's effective name by their ID, if it fails, just get their ID instead.
     *
     * @param l The ID.
     * @param g The guild to check against.
     * @return Their effect name, or ID if they don't exist.
     */
    private String tryGetEffectiveMemberNameById(long l, Guild g) {
        try {
            return g.getMemberById(l).getEffectiveName();
        } catch (Exception ex) {
            return String.valueOf(l);
        }
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
        parser.addArgument("-d", "--id")
                .action(Arguments.storeTrue())
                .help("include the internal quote ID");
        parser.addArgument("quoteAndAuthor")
                .help("quote content and quote attribution to search by (wrap with ` to do a regex search)")
                .nargs("*");
        return parser;
    }
}
