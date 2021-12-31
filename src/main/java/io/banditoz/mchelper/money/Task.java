package io.banditoz.mchelper.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;

public enum Task {
    WORK;

    private static final Random rand = new SecureRandom();

    /**
     * Returns the delay of this Task in seconds.
     *
     * @return The delay in seconds.
     */
    public int getDelay() {
        return switch (this) {
            case WORK -> 86400; // seconds in one day
            default -> 0;
        };
    }

    public BigDecimal getRandomAmount() {
        return switch (this) {
            case WORK -> BigDecimal.valueOf((rand.nextDouble() * (1500 - 500)) + 500).setScale(2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO;
        };
    }
}
