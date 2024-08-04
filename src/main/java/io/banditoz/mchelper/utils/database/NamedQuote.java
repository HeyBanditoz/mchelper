package io.banditoz.mchelper.utils.database;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which represents a user added quote from the database. Quotes must be in this format:
 * <pre>"Quote content." Quote author</pre>
 * or else the parsing will fail. Any leading dashes in the author field will be removed.
 */
public class NamedQuote {
    /** The guild this quote came from. Used for filtering the right guild when a user requests a quote. */
    private long guildId;
    /** The ID of the author who wrote this quote. */
    private long authorId;
    /** The quote itself. */
    private String quote;
    /** The author this quote is attributed to. This is not the same as the authorId. */
    private String quoteAuthor;
    /** The Timestamp of when this quote was created in the database. */
    private Timestamp lastModified;
    /** The ID of the quote as it appears in the database. */
    private int id;
    /** Special flags this quote has. */
    private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

    private final static DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMMM uuuu");
    private final static Pattern QUOTE_PARSER = Pattern.compile("^\"(.*?)\"\\s+");
    private final static String GREEDY_DASHES = "^-+";

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getQuoteAuthor() {
        return quoteAuthor;
    }

    public void setQuoteAuthor(String quoteAuthor) {
        this.quoteAuthor = quoteAuthor;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EnumSet<Flag> getFlags() {
        return flags;
    }

    public void setFlags(EnumSet<Flag> flags) {
        this.flags = flags;
    }

    /**
     * Attempts to build a NamedQuote given a String.
     *
     * @param s The String to parse.
     * @return A NamedQuote from the String.
     * @throws IllegalArgumentException if there was an error parsing the quote.
     */
    public static NamedQuote parseString(String s) {
        // fuck you, apple!
        s = fixAppleOddities(s);

        NamedQuote nq = new NamedQuote();
        Matcher m = QUOTE_PARSER.matcher(s);
        if (m.find()) {
            // replace the quotation marks with nothing inside the quote body
            nq.setQuote(m.group().replace("\"", "").trim());
            // replace extra dashes in the author field with nothing
            nq.setQuoteAuthor(m.replaceFirst("").replaceFirst(GREEDY_DASHES, ""));
        }
        else {
            throw new IllegalArgumentException("Could not create named quote from string!");
        }
        return nq;
    }

    public void editContent(String quote, String quoteAuthor) {
        quote = fixAppleOddities(quote).trim();
        quoteAuthor = fixAppleOddities(quoteAuthor).trim().replaceFirst(GREEDY_DASHES, "");

        this.quote = quote;
        this.quoteAuthor = quoteAuthor;
    }

    /**
     * Attempts the build a Quote from a given message ID that happened in the given TextChannel..
     *
     * @param id The ID of the Message to attempt to parse.
     * @param c  The TextChannel the message happened in.
     * @return A NamedQuote object.
     */
    public static NamedQuote parseMessageId(long id, TextChannel c) {
        Message m = c.retrieveMessageById(id).complete();
        NamedQuote nq = new NamedQuote();
        nq.setQuote(fixAppleOddities(m.getContentDisplay()));
        nq.setQuoteAuthor(fixAppleOddities(m.getAuthor().getName()));
        nq.setGuildId(c.getGuild().getIdLong());
        return nq;
    }

    /**
     * Formats the quote for use in Discord.
     *
     * @return The formatted String.
     */
    public String format(boolean includeId) {
        if (includeId) {
            return this.getQuote() + "\n\n*" + this.getQuoteAuthor() + " — " + TimeFormat.DATE_LONG.format(this.getLastModified().toInstant()) + "* (#" + id + ")";
        }
        else {
            return this.getQuote() + "\n\n*" + this.getQuoteAuthor() + " — " + TimeFormat.DATE_LONG.format(this.getLastModified().toInstant()) + "*";
        }
    }

    /**
     * Formats the quote for use in Discord, without the quote text (assumes it's an image instead.)
     *
     * @return The formatted String.
     */
    public String formatWithoutQuote(boolean includeId) {
        if (includeId) {
            return "*" + this.getQuoteAuthor() + " — " + TimeFormat.DATE_LONG.format(this.getLastModified().toInstant()) + "* (#" + id + ")";
        }
        else {
            return "*" + this.getQuoteAuthor() + " — " + TimeFormat.DATE_LONG.format(this.getLastModified().toInstant()) + "*";
        }
    }

    @Override
    public String toString() {
        return "“" + this.getQuote() + "” --" + this.getQuoteAuthor();
    }

    private static String fixAppleOddities(String s) {
        return s.replace('“', '"')     // U+201C
                .replace('”', '"')     // U+201D
                .replace('‘', '\'')    // U+2018
                .replace('’', '\'')    // U+2019
                .replace("…", "...");  // U+2026 Horizontal Ellipsis
    }

    /** Quote flags. Don't edit the position of existing values! */
    public enum Flag {
        /** This quote has been effectively deleted. */
        HIDDEN,
        /** This quote cannot be chosen for its guild's quote of the day. */
        EXCLUDE_QOTD,
        /**
         * This quote will be ranked lower than other quotes.
         * When searching, all <code>DERANK</code>ed quotes are
         * sorted behind non-<code>DERANK</code>ed ones.
         */
        DERANK
    }
}
