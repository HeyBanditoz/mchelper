package io.banditoz.mchelper.money.lottery;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.banditoz.mchelper.money.AccountManager.format;

import com.google.common.annotations.VisibleForTesting;
import io.avaje.inject.PostConstruct;
import io.banditoz.mchelper.database.Lottery;
import io.banditoz.mchelper.database.LotteryEntrant;
import io.banditoz.mchelper.database.StatPoint;
import io.banditoz.mchelper.database.dao.LotteryDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.utils.RandomCollection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
public class LotteryManager {
    private final LotteryDao dao;
    private final AccountManager am;
    private final ScheduledExecutorService ses;
    private final JDA jda;
    private static final Logger logger = LoggerFactory.getLogger(LotteryManager.class);

    @Inject
    public LotteryManager(LotteryDao dao,
                          AccountManager accountManager,
                          ScheduledExecutorService scheduledExecutorService,
                          JDA jda) {
        this.dao = dao;
        this.am = accountManager;
        this.ses = scheduledExecutorService;
        this.jda = jda;
    }

    @PostConstruct
    public void initialize() {
        List<Lottery> lotteries;
        try {
            lotteries = dao.getAllActiveLotteries();
        } catch (SQLException e) {
            logger.error("Could not get active lotteries.", e);
            throw new RuntimeException(e);
        }
        for (Lottery l : lotteries) {
            Duration duration = Duration.between(Instant.now(), l.drawAt().toInstant());
            ses.schedule(() -> payout(l), duration.getSeconds(), TimeUnit.SECONDS);
        }
        logger.info("Scheduled lottery payouts for " + lotteries.size() + " lotteries.");
    }

    /**
     * Begins a lottery for a guild.<br>
     * The lottery limit is currently the average of all balances in a guild, divided by the number of accounts in
     * a guild. This is (hopefully) to ensure one entrant cannot easily take all the money.
     *
     * @param c The {@link Guild} contained within the {@link TextChannel} to start {@link Lottery} for.
     * @return The maximum bet an entrant can do.
     * @throws SQLException If there was a database error.
     */
    public BigDecimal startLotteryForGuild(TextChannel c) throws SQLException {
        Guild g = c.getGuild();
        synchronized (this) {
            BigDecimal max = getTicketLimitForGuild(g);
            dao.createLottery(c, max);
            logger.info("Lottery started for guild {} with maximum ticket amount of ${}.", g, AccountManager.format(max));
            return max;
        }
    }

    public BigDecimal enterMember(BigDecimal amount, Member m) throws Exception {
        Guild g = m.getGuild();
        amount = am.scale(amount);
        synchronized (this) {
            am.checkUserCanCompleteTransaction(m.getUser(), amount);
            Lottery activeLottery = dao.getActiveLottery(g);
            if (amount.compareTo(activeLottery.limit()) > 0) {
                throw new IllegalArgumentException("Requested ticket of $" + format(amount) + " breaches lottery limit of $" + format(activeLottery.limit()));
            }
            dao.enterLottery(m, amount);
            if (dao.countParticipantsForLottery(activeLottery.id()) == 2) {
                scheduleCountdown(activeLottery);
            }
            return am.remove(amount, m.getIdLong(), "lottery ticket for " + activeLottery.id());
        }
    }

    public long calculateLotteryWinner(List<LotteryEntrant> entrants) {
        Map<Long, Double> realEntrants = new HashMap<>();
        for (LotteryEntrant entrant : entrants) {
            realEntrants.put(entrant.userId(), entrant.amount().doubleValue());
        }
        RandomCollection<Long> c = new RandomCollection<>(realEntrants);
        return c.next();
    }

    public List<LotteryEntrant> getEntrantsForLottery(Guild g) throws SQLException {
        return dao.getEntrantsForLottery(g);
    }

    public Lottery getLottery(Guild g) throws SQLException {
        return dao.getActiveLottery(g);
    }

    /**
     * Calculates the ticket limit for a guild. The formula is as follows:<br>
     * Divide the <i>average</i> account value of the guild by the <i>number of accounts</i> in the guild.
     *
     * @param g The {@link Guild} to calculate the limit for.
     * @return The ticket limit.
     * @throws SQLException If there was a database error.
     */
    public BigDecimal getTicketLimitForGuild(Guild g) throws SQLException {
        List<StatPoint<String>> topBals = am.getTopBalancesForGuild(g);
        double avg = topBals.stream()
                .mapToDouble(value -> value.getCount().doubleValue())
                .filter(value -> value > 1000)
                .average()
                .orElse(0);
        return am.scale(new BigDecimal(avg / topBals.size()));
    }

    private void payout(Lottery l) {
        Guild g = jda.getGuildById(l.guildId());
        if (g != null) {
            try {
                List<LotteryEntrant> entrants = dao.getEntrantsForLottery(g);
                BigDecimal winningAmount = entrants.stream()
                        .map(LotteryEntrant::amount)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                long winningUser = calculateLotteryWinner(entrants);
                LotteryEntrant winningEntrant = entrants.stream()
                        .filter(lotteryEntrant -> lotteryEntrant.userId() == winningUser)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("WTF?"));
                double percentChance = winningEntrant.amount().doubleValue() / winningAmount.doubleValue() * 100;
                am.add(winningAmount, winningUser, "lottery winnings for " + l.id());
                dao.markLotteryComplete(l.id());
                // now that we gave them money, run the methods that could fail
                TextChannel channel = jda.getChannelById(TextChannel.class, l.channelId());
                Member winner = g.getMemberById(winningUser);
                if (channel != null && winner != null) {
                    channel.sendMessage("Good job %s, you won $%s, against %d others, of which you had a %.2f%% chance of winning.".formatted(winner.getAsMention(), format(winningAmount), entrants.size() - 1 ,percentChance)).queue();
                }
                logger.info("Lottery ended for guild {} with {} as the winner, of {}.", g, winningUser, winningEntrant.amount());
            } catch (Exception ex) {
                logger.error("Exception thrown when paying out for lottery " + l.id(), ex);
            }
        }
    }
    
    @VisibleForTesting
    public void scheduleCountdown(Lottery activeLottery) {
        Duration duration = Duration.between(Instant.now(), activeLottery.drawAt().toInstant());
        ses.schedule(() -> payout(activeLottery), duration.getSeconds(), TimeUnit.SECONDS);
        logger.info("Lottery countdown started for lottery {}. Duration {}.", activeLottery, duration);
    }
}
