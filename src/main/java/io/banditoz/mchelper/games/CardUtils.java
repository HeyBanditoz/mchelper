package io.banditoz.mchelper.games;

import java.util.ArrayList;
import java.util.List;

public class CardUtils {
    public static List<Card> buildStandardDeck(int decks) {
        if (decks < 1) {
            throw new IllegalArgumentException("must build at least one deck!");
        }
        List<Card> cards = new ArrayList<>(52 * decks);
        for (int i = 0; i < decks; i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(suit, rank));
                }
            }
        }
        return cards;
    }
}
