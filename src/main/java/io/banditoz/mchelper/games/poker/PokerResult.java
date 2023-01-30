package io.banditoz.mchelper.games.poker;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PokerResult {
    ROYAL_FLUSH(800, 11),
    STRAIGHT_FLUSH(50, 10),
    SKIP_STRAIGHT_FLUSH_ACE_LOW_TMP(0, 9),
    FOUR_OF_A_KIND(25, 8),
    FULL_HOUSE(9, 7),
    FLUSH(6, 6),
    STRAIGHT(4, 5),
    SKIP_STRAIGHT_ACE_LOW_TMP(0, 4),
    THREE_OF_A_KIND(3,  3),
    TWO_PAIR(2, 2),
    JACKS_OR_BETTER(1, -1),
    // below are not used in other poker scoring, neither are the multiplier values
    ONE_PAIR(0, 1),
    HIGH_CARD(0, 0),
    LOSS(0, -1);

    private final int multiplier;
    private final int scoringEngineRank;
    private final static Map<Integer, PokerResult> fromRank = new HashMap<>();

    static {
        for (PokerResult res : EnumSet.allOf(PokerResult.class)) {
            if (res.scoringEngineRank != -1) {
                fromRank.put(res.scoringEngineRank, res);
            }
        }
    }

    public static PokerResult fromRank(int rank) {
        return fromRank.get(rank);
    }

    PokerResult(int multiplier, int scoringEngineRank) {
        this.multiplier = multiplier;
        this.scoringEngineRank = scoringEngineRank;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getScoringEngineRank() {
        return scoringEngineRank;
    }
}
