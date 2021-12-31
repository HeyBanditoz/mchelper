package io.banditoz.mchelper.utils.database;

public enum Type {
    /** This transfer was between two accounts. */
    TRANSFER(0),
    /** The bot granted this person money. */
    GRANT(1),
    /** The bot revoked money. */
    REVOKE(2);

    private final int value;

    Type(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this {@link Type}.
     *
     * @return The integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the String value of this {@link Type}.
     *
     * @return The String value.
     */
    public String getStringValue() {
        return switch (this) {
            case TRANSFER -> "Transfer";
            case GRANT -> "Grant";
            case REVOKE -> "Revoke";
        };
    }
}
