package io.banditoz.mchelper.stats;

/**
 * Represents how any executable finished running.
 */
public enum Status {
    /** The executable ran successfully. */
    SUCCESS(0),
    /** The executable failed. (i.e. the user did not get what they were looking for, et al.) */
    FAIL(1),
    /** The executable failed with an exception. */
    EXCEPTIONAL_FAILURE(2),
    /** The executable was on cooldown. */
    COOLDOWN(3),
    /** The user has no permissions to execute this command. */
    NO_PERMISSION(4),
    /** While trying to execute this executable, the user did not have bot owner permissions. */
    BOT_OWNER_CHECK_FAILED(5);

    private final int value;

    Status(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this {@link Status}.
     * @return The integer value.
     */
    public int getValue() {
        return value;
    }


}