package io.banditoz.mchelper;

import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import io.banditoz.mchelper.utils.database.dao.LevelDao;
import io.banditoz.mchelper.utils.database.dao.LevelDaoImpl;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.*;

public class LevelListener extends ListenerAdapter {
    private final MCHelper mcHelper;
    private final AccountManager am;
    /** I know I said not to use AccountsDao, it has a queryBalance that won't throw. */
    private final AccountsDao accountsDao;
    private final LevelDao levelDao;
    /** ANTISPAM SUPPORT */
    private final Map<User, Instant> messageLastSent = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(LevelListener.class);

    public LevelListener(MCHelper mcHelper) {
        this.mcHelper = mcHelper;
        this.am = mcHelper.getAccountManager();
        this.accountsDao = new AccountsDaoImpl(mcHelper.getDatabase());
        this.levelDao = new LevelDaoImpl(mcHelper.getDatabase());
        log.info("April Fools 2023 initialized.");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User u = event.getAuthor();
        if (!event.isFromGuild()) {
            return;
        }
        if (u.isBot()) {
            return;
        }
        if (u.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }
        if (!event.getChannel().canTalk()) {
            return;
        }

        mcHelper.getThreadPoolExecutor().execute(() -> {
            try {
                int level = levelDao.getLevel(u.getIdLong());
                Instant lastSent = messageLastSent.put(u, Instant.now());
                if (lastSent == null) {
                    levelDao.incrementUser(u.getIdLong(), true);
                }
                // ADVANCED antispam algorithm below!
                else if (Instant.now().isAfter(lastSent.plus(level + 5, ChronoUnit.SECONDS))) {
                    // ADVANCED leveling algorithm below!
                    int levelableMessages = levelDao.incrementUser(u.getIdLong(), true);
                    double v = floor((pow(level, 1.25) * 4.0) + 5.0);
                    if (levelableMessages > v) {
                        levelDao.levelUp(u.getIdLong());
                        log.info("{} leveled up to {}.", u, (level + 1));
                        BigDecimal levelUpEarnings = BigDecimal.valueOf(sqrt(50 * v) + 25);
                        String formattedEarnings = AccountManager.format(levelUpEarnings);
                        if (accountsDao.queryBalance(u.getIdLong(), false) != null) {
                            event.getMessage().reply("Congratulations! You leveled up to level " + (level + 1) + "! " +
                                            "For your valiant efforts, you have been rewarded $" + formattedEarnings + ".")
                                    .queue();
                            am.add(levelUpEarnings, u.getIdLong(), "level up to " + (level + 1));
                        } else {
                            event.getMessage().reply("Congratulations! You leveled up to level " + (level + 1) + "! " +
                                            "If you had an account, you would have been rewarded $" + formattedEarnings)
                                    .queue();
                        }
                    }
                } else {
                    levelDao.incrementUser(u.getIdLong(), false);
                }
            } catch (Exception ex) {
                log.error("Could not handle user level.", ex);
            }
        });
    }
}
