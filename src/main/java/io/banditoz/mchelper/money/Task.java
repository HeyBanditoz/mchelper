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
        switch (this) {
            case WORK:
                return 86400; // seconds in one day
            default:
                return 0;
        }
    }

    public BigDecimal getRandomAmount() {
        switch (this) {
            case WORK:
                return BigDecimal.valueOf((rand.nextDouble() * (1500 - 500)) + 500).setScale(2, RoundingMode.HALF_UP);
            default:
                return BigDecimal.ZERO;
        }
    }
}
