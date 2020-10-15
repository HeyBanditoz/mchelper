package io.banditoz.mchelper.utils.database;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
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
    private final static DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMMM uuuu");
    private final static Pattern QUOTE_PARSER = Pattern.compile("\"(.*?)\"\\s+");
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

    /**
     * Attempts to build a NamedQuote given a String.
     *
     * @param s The String to parse.
     * @return A NamedQuote from the String.
     * @throws IllegalArgumentException if there was an error parsing the quote.
     */
    public static NamedQuote parseString(String s) {
        // fuck you, apple!
        s = s.replace('“', '"').replace('”', '"');

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
        nq.setQuote(m.getContentDisplay());
        nq.setQuoteAuthor(m.getAuthor().getName());
        nq.setGuildId(c.getGuild().getIdLong());
        return nq;
    }

    /**
     * Formats the quote for use in Discord.
     *
     * @return The formatted String.
     */
    public String format() {
        return this.getQuote() + "\n\n*" + this.getQuoteAuthor() + " — " + DATE.format(this.getLastModified().toLocalDateTime()) + '*';
    }
}
