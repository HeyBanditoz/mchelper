package io.banditoz.mchelper.games;

public class Card {
    private final Suit SUIT;
    private final Rank RANK;

    public Card(Suit suit, Rank rank) {
        this.SUIT = suit;
        this.RANK = rank;
    }

    @Override
    public String toString() {
        return SUIT.getStringValue() + RANK.getStringValue();
    }
}
