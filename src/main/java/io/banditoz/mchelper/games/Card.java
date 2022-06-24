package io.banditoz.mchelper.games;

public record Card(Suit suit, Rank rank) {
    @Override
    public String toString() {
        return suit.getStringValue() + rank.getStringValue();
    }
}
