package io.banditoz.mchelper.games;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.MoneyException;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public abstract class Game {
    protected final BigDecimal low;
    protected final BigDecimal high;
    protected final GameManager gm;
    protected final AccountManager am;
    protected final User player;
    protected final BigDecimal ante;
    protected final MCHelper mcHelper;
    public static final BigDecimal TWO = new BigDecimal("2");
    public static final BigDecimal THREE = new BigDecimal("3");
    protected final Logger LOGGER;

    Game(int low, int high, MCHelper mcHelper, User u, BigDecimal ante) {
        this.low = new BigDecimal(low);
        this.high = new BigDecimal(high);
        this.gm = mcHelper.getGameManager();
        this.am = mcHelper.getAccountManager();
        this.player = u;
        this.ante = ante;
        this.mcHelper = mcHelper;
        this.LOGGER = LoggerFactory.getLogger(this.getClass());
    }

    public void stopPlaying() {
        gm.stopPlaying(player);
    }

    public void startPlaying() {
        gm.startPlaying(player);
    }

    /**
     * Checks if a given {@link User} can play, and throws an exception if not.
     * Also checks if the amount of money they're putting down as an ante is within the range of the game.<br>
     * If both succeed, the balance will be subratcted from the user.
     *
     * @throws MoneyException If their ante is off, or they're in a game already.
     */
    public void tryAndRemoveAnte(String memo) throws Exception {
        if (!(ante.compareTo(low) >= 0 && ante.compareTo(high) <= 0)) {
            throw new MoneyException("Your bet must be between " + low + " and " + high + "!");
        }
        if (gm.isPlaying(player)) {
            throw new MoneyException("You're already playing a game.");
        }
        try {
            am.remove(ante, player.getIdLong(), memo);
        } finally {
            stopPlaying();
        }
    }
}
