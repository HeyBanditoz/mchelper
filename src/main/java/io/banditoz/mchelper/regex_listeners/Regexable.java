package io.banditoz.mchelper.regex_listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public abstract class Regexable {
    protected abstract void onRegexCommand(RegexCommandEvent rce) throws Exception;
    protected abstract Pattern regex();
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Whether or not the passed in String passes regex.
     *
     * @param args The args to check against implemented {@link Regexable}'s {@link java.util.regex.Matcher}.
     * @return Whether or not the regex matches.
     */
    public boolean containsRegexable(String args) {
        return regex().matcher(args).find();
    }
}
