package io.banditoz.mchelper.utils.database;

import io.banditoz.mchelper.money.AccountManager;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StatPointTests {
    private final List<StatPoint<String, BigDecimal>> amounts;

    public StatPointTests() {
        List<StatPoint<String, BigDecimal>> localAmounts = new ArrayList<>(List.of(
                new StatPoint<>("Mr. Foo", new BigDecimal("5.352")),
                new StatPoint<>("Ms. Bar", new BigDecimal("2.689")),
                new StatPoint<>("Mrs. Baz", new BigDecimal("19.23"))));
        Collections.sort(localAmounts);
        this.amounts = localAmounts;
    }

    @Test
    public void testStatPointLeaderboardGeneration() {
        String leaderboard = StatPoint.statsToPrettyLeaderboard(amounts,
                10,
                s -> s,
                BigDecimal::toPlainString);
        Assertions.assertThat(leaderboard).isEqualTo("\n```\n" +
                "Rank  Name\n" +
                "1.    Mrs. Baz       19.23\n" +
                "2.    Mr. Foo        5.352\n" +
                "3.    Ms. Bar        2.689\n" +
                "```");
    }

    @Test
    public void testStatPointLeaderboardGenerationDifferentCountFormatter() {
        String leaderboard = StatPoint.statsToPrettyLeaderboard(amounts,
                10,
                s -> s,
                AccountManager::format);
        Assertions.assertThat(leaderboard).isEqualTo("\n```\n" +
                "Rank  Name\n" +
                "1.    Mrs. Baz       19.23\n" +
                "2.    Mr. Foo        5.35\n" +
                "3.    Ms. Bar        2.69\n" +
                "```");
    }

    @Test
    public void testStatPointLeaderboardGenerationThingFormatter() {
        String leaderboard = StatPoint.statsToPrettyLeaderboard(amounts,
                10,
                s -> s.toUpperCase(Locale.ROOT),
                BigDecimal::toPlainString);
        Assertions.assertThat(leaderboard).isEqualTo("\n```\n" +
                "Rank  Name\n" +
                "1.    MRS. BAZ       19.23\n" +
                "2.    MR. FOO        5.352\n" +
                "3.    MS. BAR        2.689\n" +
                "```");
    }

    @Test
    public void testStatPointLeaderboardGenerationStringTruncation() {
        String leaderboard = StatPoint.statsToPrettyLeaderboard(amounts,
                2,
                s -> s,
                BigDecimal::toPlainString);
        Assertions.assertThat(leaderboard).isEqualTo("\n```\n" +
                "Rank  Name\n" +
                "1.    Mr...  19.23\n" +
                "2.    Mr...  5.352\n" +
                "3.    Ms...  2.689\n" +
                "```");
    }
}
