package io.banditoz.mchelper.utils.database;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedQuote {
    private long guildId;
    private long authorId;
    private String quote;
    private String quoteAuthor;
    private Timestamp lastModified;

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

    public static NamedQuote parseString(String s) {
        // fuck you, apple!
        s = s.replace('“', '"').replace('”', '"');

        NamedQuote nq = new NamedQuote();
        Pattern p = Pattern.compile("\"(.*?)\"\\s+");
        Matcher m = p.matcher(s);
        if (m.find()) {
            nq.setQuote(m.group().replace("\"", "").trim());
            nq.setQuoteAuthor(m.replaceFirst(""));
        }
        else {
            throw new IllegalArgumentException("Could not create named quote from string!");
        }
        return nq;
    }

    public String format() {
        return "“" + this.getQuote() + "” --" + this.getQuoteAuthor();
    }
}
