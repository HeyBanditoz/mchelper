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
    public long getDelay() {
        return switch (this) {
            case WORK -> 21600; // 6 hours
//            case WORK -> {
//                Calendar c = Calendar.getInstance();
//                c.set(Calendar.HOUR_OF_DAY, 0);
//                c.set(Calendar.MINUTE, 0);
//                c.set(Calendar.SECOND, 0);
//                c.set(Calendar.MILLISECOND, 0);
//                c.add(Calendar.DAY_OF_WEEK, 1);
//                c.add(Calendar.SECOND, 5); // hacky, but will show 00:00 instead of 23:59
//                yield (c.getTimeInMillis() - Instant.now().toEpochMilli()) / 1000;
//            }
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
