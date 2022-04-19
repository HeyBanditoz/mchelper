package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class QotdRunnable implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(QotdRunnable.class);
    private final MCHelper MCHELPER;

    public QotdRunnable(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    @Override
    public void run() {
        LOGGER.info("Sending quote of the day...");
        QuotesDao dao = new QuotesDaoImpl(MCHELPER.getDatabase());
        int i = 0;
        for (GuildConfig guild : new GuildConfigDaoImpl(MCHELPER.getDatabase()).getAllGuildConfigs()) {
            if (guild.getPostQotdToDefaultChannel()) {
                i++;
                // getting the guild from the cache sucks, but if it fails we probably aren't even in their guild anymore
                Guild g = MCHELPER.getJDA().getGuildById(guild.getId());
                if (g != null) {
                    try {
                        NamedQuote nq = dao.getRandomQuote(g);
                        if (nq != null) {
                            g.getTextChannelById(guild.getDefaultChannel()).sendMessageEmbeds(formatQuote(nq)).queue();
                        }
                    } catch (Exception e) {
                        LOGGER.error("Could not send QOTD to " + guild.getId() + " as there was an exception fetching one or sending to the channel.", e);
                    }
                }
                else {
                    LOGGER.warn("Could not send QOTD to " + guild.getId() + " as it doesn't exist in the cache.");
                }
            }
        }
        LOGGER.info("Delivered quote of the day to " + i + " guild(s).");
    }

    private MessageEmbed formatQuote(NamedQuote nq) {
        return new EmbedBuilder().setTitle("Quote of the Day").setColor(Color.GREEN).setDescription(nq.format(false)).build();
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
