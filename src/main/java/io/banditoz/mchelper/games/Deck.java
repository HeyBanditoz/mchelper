package io.banditoz.mchelper.games;

import java.util.*;

public class Deck {
    private LinkedList<Card> cards;
    private LinkedList<Card> used = new LinkedList<>();
    private double shufflePercent;
    private int capacity;

    public Deck() {
        cards = buildStandardDeck(1);
        shufflePercent = 0;
        Collections.shuffle(cards);
    }

    public Deck(int decks) {
        cards = buildStandardDeck(decks);
        shufflePercent = 0;
        Collections.shuffle(cards);
    }

    public Deck(double shufflePercent) {
        cards = buildStandardDeck(1);
        this.shufflePercent = shufflePercent;
        Collections.shuffle(cards);
    }

    public Deck(int decks, double shufflePercent) {
        this.cards = buildStandardDeck(decks);
        this.shufflePercent = shufflePercent;
        Collections.shuffle(cards);
    }

    public void shuffle() {
        while (!used.isEmpty()) {
            cards.push(used.pop());
        }
        Collections.shuffle(cards);
    }

    public Card draw() {
        if ((cards.size()/(double)capacity)<=shufflePercent) {
            shuffle();
        }
        Card temp = cards.pop();
        used.push(temp);
        return temp;
    }

    public int getRemainingCards() {
        return cards.size();
    }

    private LinkedList<Card> buildStandardDeck(int decks) {
        if (decks < 1) {
            throw new IllegalArgumentException("must build at least one deck!");
        }
        capacity = 52 * decks;
        LinkedList<Card> cards = new LinkedList<>();
        for (int i = 0; i < decks; i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.push(new Card(suit, rank));
                }
            }
        }
        return cards;
    }
}
