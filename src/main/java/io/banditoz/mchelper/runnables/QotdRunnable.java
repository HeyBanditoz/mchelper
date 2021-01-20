package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import io.banditoz.mchelper.utils.quotes.QotdFetcher;
import io.banditoz.mchelper.utils.quotes.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class QotdRunnable implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(QotdRunnable.class);
    private final MCHelper MCHELPER;

    public QotdRunnable(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    @Override
    public void run() {
        LOGGER.info("Sending quote of the day...");
        String quoteOfTheDay = "There was most likely an exception while fetching the quote of the day. Check the logs.";
        try {
            quoteOfTheDay = formatQuote(new QotdFetcher(MCHELPER).getQotd());
        } catch (Exception e) {
            LOGGER.error("Could not fetch the quote of the day!", e);
        }
        int i = 0;
        for (GuildConfig guild : new GuildConfigDaoImpl(MCHELPER.getDatabase()).getAllGuildConfigs()) {
            if (guild.getPostQotdToDefaultChannel()) {
                i++;
                CommandUtils.sendReply("Here is your quote of the day:\n" + quoteOfTheDay, MCHELPER.getJDA().getTextChannelById(guild.getDefaultChannel()));
            }
        }
        LOGGER.info("Delivered quote of the day to " + i + " guild(s).");
    }

    private String formatQuote(Quote qi) {
        return "“" + qi.getBody() + "” --" + qi.getAuthor();
    }

    public static Duration getDelay() {
        // TODO Make this a config instead of hardcoded timezones and times. Perhaps per guild?
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Denver"));
        ZonedDateTime nextRun = now.withHour(9).withMinute(0).withSecond(0);
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun);
    }
}