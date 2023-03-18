package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class QotdRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(QotdRunnable.class);
    private final JDA jda;
    private final ConfigurationProvider configs;
    private final QuotesDao quotesDao;

    public QotdRunnable(MCHelper mcHelper) {
        this.jda = mcHelper.getJDA();
        this.configs = new ConfigurationProvider(mcHelper.getDatabase());
        this.quotesDao = new QuotesDaoImpl(mcHelper.getDatabase());
    }

    @Override
    public void run() {
        log.info("Sending quote of the day...");
        AtomicInteger i = new AtomicInteger();
        Map<Long, String> guildChannelsToSend = new HashMap<>();
        // TODO fix this shit to be faster
        try {
            List<Long> guildsWithQotd = new ArrayList<>();
            configs.getAllGuildsWith(Config.POST_QOTD_TO_DEFAULT_CHANNEL)
                    .forEach((k, v) -> {
                        if (v != null && v.equalsIgnoreCase("true")) {
                            guildsWithQotd.add(k);
                        }
                    });
            configs.getAllGuildsWith(Config.DEFAULT_CHANNEL)
                    .forEach((k, v) -> {
                        if (v != null && guildsWithQotd.contains(k)) {
                            guildChannelsToSend.put(k, v);
                        }
                    });
        } catch (Exception e) {
            log.error("Encountered Exception while updating all voice roles.", e);
            return;
        }
        guildChannelsToSend.forEach((k, v) -> {
            Guild guild = jda.getGuildById(k);
            if (guild != null) {
                try {
                    NamedQuote nq = quotesDao.getRandomQuote(guild);
                    if (nq != null) {
                        guild.getTextChannelById(v).sendMessageEmbeds(formatQuote(nq)).queue();
                        i.getAndIncrement();
                    }
                } catch (Exception e) {
                    log.error("Could not send QOTD to " + guild.getId() + " as there was an exception fetching one or sending to the channel.", e);
                }
            }
            else {
                log.warn("Could not send QOTD to " + guild.getId() + " as it doesn't exist in the cache.");
            }
        });
        log.info("Delivered quote of the day to " + i + " guild(s).");
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
