package io.banditoz.mchelper.money;

import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Yikes! This class is super kludgy, but oh well. It's April Fools!
 * We really have no way to carry state between the command and the button press <i>from</i> a command.
 */
@Singleton
public class TipModifierCache {
    /** Users and their next modifier due to the tip they had selected. */
    private final Map<Long, BigDecimal> CACHED_MULTIPLIERS = new HashMap<>();

    public void put(long user, BigDecimal multiplier) {
        // proper multithreaded map impls or lazy synchronized block
        // call it
        synchronized (this) {
            CACHED_MULTIPLIERS.put(user, multiplier);
        }
    }

    public BigDecimal get(long user) {
        synchronized (this) {
            BigDecimal multiplier = CACHED_MULTIPLIERS.remove(user);
            return Objects.requireNonNullElse(multiplier, BigDecimal.ONE);
        }
    }
}
