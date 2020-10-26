package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.regex.Pattern;

public abstract class Regexable {
    protected abstract Status onRegexCommand(RegexCommandEvent rce) throws Exception;
    protected abstract Pattern regex();
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Return this regexable's <i>channel-wide</i> cooldown in seconds. Override and return what you want the cooldown to be.
     *
     * @return The cooldown.
     */
    protected int getCooldown() {
        return 0;
    }

    protected final HashMap<String, Instant> cooldowns = getCooldown() > 0 ? new HashMap<>() : null;

    /**
     * Whether or not the passed in String passes regex.
     *
     * @param args The args to check against implemented {@link Regexable}'s {@link java.util.regex.Matcher}.
     * @return Whether or not the regex matches.
     */
    public boolean containsRegexable(String args) {
        return regex().matcher(args).find();
    }

    /**
     * Returns the {@link Pattern} associated with this {@link Regexable}.
     *
     * @return The {@link Pattern}.
     */
    public Pattern getPattern() {
        return this.regex();
    }

    /**
     * Checks if the channel is on cooldown.
     *
     * @param id The ID to check.
     * @return true if the channel are allowed to run the regexable, false if they are still on cooldown.
     */
    protected boolean handleCooldown(String id) {
        // TODO no copypaste :(
        if (getCooldown() > 0) {
            Instant cooldown = cooldowns.get(id);
            if (cooldown == null) {
                Instant instant = Instant.now().plus(getCooldown(), ChronoUnit.SECONDS);
                cooldowns.put(id, instant);
                return true;
            }
            else if (Instant.now().isAfter(cooldown)) {
                cooldowns.replace(id, Instant.now().plus(getCooldown(), ChronoUnit.SECONDS));
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }
}
