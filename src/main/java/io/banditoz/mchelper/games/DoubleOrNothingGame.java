package io.banditoz.mchelper.games;

import io.banditoz.mchelper.money.AccountManager;
import net.dv8tion.jda.api.entities.User;

import java.math.BigDecimal;
import java.util.Random;

public class DoubleOrNothingGame {
    private BigDecimal currentBet;
    private final Random rand = new Random();
    private final AccountManager accs;
    private int times = 0;
    private final User player;
    private static final BigDecimal TWO = new BigDecimal("2");

    public DoubleOrNothingGame(BigDecimal initialBet, User player, AccountManager accs) {
        this.currentBet = initialBet;
        this.player = player;
        this.accs = accs;
    }

    /**
     * Play double or nothing.
     *
     * @return True if they doubled, false if they lost it all!
     */
    public boolean play() {
        if (rand.nextDouble() <= 0.51) {
            currentBet = currentBet.multiply(TWO); // fairly double your money (51% statistically,) but maybe not so in the future?
            times++;
            return true;
        }
        else {
            return false;
        }
    }

    public void payout() throws Exception {
        accs.add(currentBet, player.getIdLong(), "double or nothing winnings (bet x" + times + ")");
    }

    public BigDecimal getCurrentBet() {
        return currentBet;
    }
}
