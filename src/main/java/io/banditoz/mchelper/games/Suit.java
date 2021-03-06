package io.banditoz.mchelper.games;

public enum Suit {
    SPADES(0),
    HEARTS(1),
    DIAMONDS(2),
    CLUBS(3);

    private final int value;

    Suit(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this {@link Suit}.
     *
     * @return The integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the String value of this {@link Suit}.
     *
     * @return The String value.
     */
    public String getStringValue() {
        switch (this) {
            case SPADES:
                return "♠";
            case HEARTS:
                return "♥";
            case DIAMONDS:
                return "♦";
            case CLUBS:
                return "♣";
        }
        return null;
    }
}
