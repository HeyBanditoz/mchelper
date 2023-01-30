package io.banditoz.mchelper.games.poker;

import io.banditoz.mchelper.games.Card;
import io.banditoz.mchelper.games.Rank;
import io.banditoz.mchelper.games.Suit;
import org.testng.annotations.Test;

import java.util.List;

import static io.banditoz.mchelper.games.poker.PokerResult.*;
import static org.assertj.core.api.Assertions.assertThat;

public class VideoPokerScoringTests {
    @Test
    public void testVideoPokerScoringJacksOrBetter_0() {
        List<Card> cs = List.of(
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.FOUR),
                new Card(Suit.SPADES, Rank.ACE)
        );
        assertThat(PokerScoringEngine.evaluateVideoPoker(cs)).isEqualTo(JACKS_OR_BETTER);
    }

    @Test
    public void testVideoPokerScoringRoyalFlush_0() {
        List<Card> cs = List.of(
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.SPADES, Rank.TEN),
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.SPADES, Rank.QUEEN)
        );
        assertThat(PokerScoringEngine.evaluateVideoPoker(cs)).isEqualTo(ROYAL_FLUSH);
    }

    @Test
    public void testVideoPokerScoringLoss_0() {
        List<Card> cs = List.of(
                new Card(Suit.SPADES, Rank.TWO),
                new Card(Suit.HEARTS, Rank.FOUR),
                new Card(Suit.SPADES, Rank.SEVEN),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.QUEEN)
        );
        assertThat(PokerScoringEngine.evaluateVideoPoker(cs)).isEqualTo(LOSS);
    }
}
