package io.banditoz.mchelper.utils.quotes;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class QotdRunnable implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(QotdRunnable.class);

    @Override
    public void run() {
        logger.info("Sending quote of the day...");
        String quoteOfTheDay = "There was most likely an exception while fetching the quote of the day. Check the logs.";
        try {
            quoteOfTheDay = formatQuote(QotdFetcher.getQotd());
        } catch (Exception e) {
            logger.error("Could not fetch the quote of the day!", e);
        }
        for (GuildConfig guild : new GuildConfigDaoImpl().getAllGuildConfigs()) {
            if (guild.getPostQotdToDefaultChannel()) {
                CommandUtils.sendReply("Here is your quote of the day:\n" + quoteOfTheDay, MCHelper.getJDA().getTextChannelById(guild.getDefaultChannel()));
            }
        }
    }

    private String formatQuote(QuoteItem qi) {
        return "“" + qi.getQuote() + "” --" + qi.getAuthor();
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
