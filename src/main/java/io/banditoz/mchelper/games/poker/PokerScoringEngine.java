package io.banditoz.mchelper.games.poker;

import io.banditoz.mchelper.games.Card;

import java.util.List;

import static io.banditoz.mchelper.games.Rank.*;
import static io.banditoz.mchelper.games.poker.PokerResult.*;

/**
 * Do not question how this class works. I'm not sure.
 *
 * @author <a href="https://github.com/alberthendriks/lpokerbot/blob/5ac19ba1f3a08c791adeda1174fad5d48cf60612/RankPokerHandPublic.java">alberthendriks</a>
 */
public class PokerScoringEngine {
    private static final PokerResult[] POSSIBLE_HANDS = new PokerResult[]{
            FOUR_OF_A_KIND, STRAIGHT_FLUSH, STRAIGHT, FLUSH, HIGH_CARD, ONE_PAIR, TWO_PAIR, ROYAL_FLUSH, THREE_OF_A_KIND, FULL_HOUSE
    };
    private final static long MASK4_1 = 0b11110000_11110000_11110000_11110000_11110000_11110000_11110000_11110000L;
    private final static long MASK4_2 = 0b00001111_00001111_00001111_00001111_00001111_00001111_00001111_00001111L;

    public static PokerResult evaluatePoker(List<Card> cards) {
        int[] nr = new int[5];
        int[] suit = new int[5];
        for (int i = 0; i < cards.size(); i++) {
            nr[i] = cards.get(i).rank().getScoringEngineValue();
            suit[i] = cards.get(i).suit().getScoringEngineValue();
        }
        int res = rankPokerHand5(nr, suit);
        return PokerResult.fromRank(res >> 26);
    }

    public static PokerResult evaluateVideoPoker(List<Card> cards) {
        PokerResult pokerResult = evaluatePoker(cards);
        if (pokerResult.equals(HIGH_CARD)) {
            return LOSS;
        }
        if (pokerResult.equals(ONE_PAIR)) {
            // jacks or better, count cards >= jacks
            // TODO this is hacky, clean it up :(
            long acesCount = cards.stream().filter(card -> card.rank() == ACE).count();
            long jacksCount = cards.stream().filter(card -> card.rank() == JACK).count();
            long queenCount = cards.stream().filter(card -> card.rank() == QUEEN).count();
            long kingCount = cards.stream().filter(card -> card.rank() == KING).count();

            if (jacksCount == 2 || acesCount == 2 || queenCount == 2 || kingCount == 2) {
                return JACKS_OR_BETTER;
            }
            else {
                return LOSS;
            }
        }
        return pokerResult;
    }

    private static int rankPokerHand5(int[] nr, int[] suit) {
        long v = 0L;
        int set = 0;
        for (int i = 0; i < 5; i++) {
            v += (v & (15L << (nr[i] * 4))) + (1L << (nr[i] * 4));
            set |= 1 << (nr[i] - 2);
        }
        int value = (int) (v % 15L - ((hasStraight(set)) || (set == 0x403c / 4) ? 3L : 1L)); // keep the v value at this point
        value -= (suit[0] == (suit[1] | suit[2] | suit[3] | suit[4]) ? 1 : 0) * ((set == 0x7c00 / 4) ? -5 : 1);
        value = POSSIBLE_HANDS[value].getScoringEngineRank();

        // break ties
        value = value << 26;
        value |= value == FULL_HOUSE.getScoringEngineRank() << 26 ? 64 - Long.numberOfLeadingZeros(v & (v << 1) & (v << 2)) << 20
                : set == 0x403c / 4 ? 0 // Ace low straights
                : ((64 - Long.numberOfLeadingZeros(
                Math.max((v & MASK4_1) & ((v & MASK4_1) << 1), (v & MASK4_2) & ((v & MASK4_2) << 1))) << 20) |
                (Long.numberOfTrailingZeros(
                        minPos((v & MASK4_1) & ((v & MASK4_1) << 1), (v & MASK4_2) & ((v & MASK4_2) << 1))) << 14));
        value |= set;
        return value;
    }

    private static long minPos(long a, long b) {
        return a == 0 ? b : b == 0 ? a : a < b ? a : b;
    }

    private static boolean hasStraight(int set) {
        return 0 != (set & (set >> 1) & (set >> 2) & (set >> 3) & (set >> 4));
    }
}
