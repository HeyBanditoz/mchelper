package io.banditoz.mchelper.games;

public enum Rank {
    ACE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this {@link Rank}.
     *
     * @return The integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the String value of this {@link Rank}.
     *
     * @return The String value.
     */
    public String getStringValue() {
        switch (this) {
            case ACE:
                return "A";
            case TWO:
                return "2";
            case THREE:
                return "3";
            case FOUR:
                return "4";
            case FIVE:
                return "5";
            case SIX:
                return "6";
            case SEVEN:
                return "7";
            case EIGHT:
                return "8";
            case NINE:
                return "9";
            case TEN:
                return "10";
            case JACK:
                return "J";
            case QUEEN:
                return "Q";
            case KING:
                return "K";
        }
        return null;
    }
}
