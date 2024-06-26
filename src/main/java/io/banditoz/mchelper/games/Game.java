package io.banditoz.mchelper.games;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.MoneyException;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Game {
    protected final BigDecimal low;
    protected final BigDecimal high;
    protected final GameManager gm;
    private final AccountManager am;
    protected final User player;
    protected final BigDecimal ante;
    private final MCHelper mcHelper;
    public static final BigDecimal TWO = new BigDecimal("2");
    public static final BigDecimal THREE = new BigDecimal("3");
    protected final Logger LOGGER;
    private final String gameId = RandomStringUtils.randomAlphanumeric(6); // 44261653680 possibilities

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
            remove(ante, player.getIdLong(), memo);
        } finally {
            stopPlaying();
        }
    }

    protected BigDecimal add(BigDecimal amount, long to, String memo) throws Exception {
        return am.add(amount, to, memo + " @" + gameId);
    }

    protected BigDecimal remove(BigDecimal amount, long from, String memo) throws Exception {
        return am.remove(amount, from, memo + " @" + gameId);
    }

    protected ScheduledExecutorService getScheduledExecutorService() {
        return mcHelper.getSES();
    }
}
