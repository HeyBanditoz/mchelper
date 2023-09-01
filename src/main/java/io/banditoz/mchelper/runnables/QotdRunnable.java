package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.motd.MotdSectionGenerator;
import io.banditoz.mchelper.motd.NewsHeadlineMotdSectionGenerator;
import io.banditoz.mchelper.motd.QotdMotdSectionGenerator;
import io.banditoz.mchelper.motd.WeatherMotdSectionGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.text.DecimalFormat;
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
    private final MotdSectionGenerator[] generators;

    public QotdRunnable(MCHelper mcHelper) {
        this.jda = mcHelper.getJDA();
        this.configs = mcHelper.getConfigurationProvider();
        this.generators = new MotdSectionGenerator[] {
                new WeatherMotdSectionGenerator(mcHelper),
                new NewsHeadlineMotdSectionGenerator(mcHelper),
                new QotdMotdSectionGenerator(mcHelper)
        };
    }

    @Override
    public void run() {
        log.info("Sending quote of the day...");
        long before = System.currentTimeMillis(); // timing
        AtomicInteger deliveryCount = new AtomicInteger();
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
            log.error("Encountered Exception while gathering channels to send the QOTD to.", e);
            return;
        }
        guildChannelsToSend.forEach((k, v) -> {
            Guild guild = jda.getGuildById(k);
            if (guild != null) {
                try {
                    TextChannel tc = guild.getTextChannelById(v);
                    List<MessageEmbed> embeds = new ArrayList<>(3);
                    for (MotdSectionGenerator g : generators) {
                        try {
                            MessageEmbed me = g.generate(tc);
                            if (me == null) {
                                throw new NullPointerException("Generated section is null");
                            }
                            embeds.add(g.generate(tc));
                        } catch (Exception e) {
                            log.error("Exception thrown generating MOTD section for " + g.getClass().getSimpleName(), e);
                            embeds.add(new EmbedBuilder()
                                    .setTitle("Exception thrown!")
                                    .setDescription("Could not generate MOTD section for " + g.getClass().getSimpleName() + ". Check the logs.")
                                    .setColor(new Color(162, 35, 35))
                                    .build());
                        }
                    }
                    tc.sendMessage(new MessageCreateBuilder()
                            .addContent("**Message of the day for** ***" + MarkdownSanitizer.escape(guild.getName()) + ":***")
                            .setEmbeds(embeds)
                            .build())
                            .queue();
                    deliveryCount.getAndIncrement();
                } catch (Exception e) {
                    log.error("Could not send QOTD to " + guild.getId() + " as there was an exception fetching one or sending to the channel.", e);
                }
            }
            else {
                log.warn("Could not send QOTD to " + k + " as it doesn't exist in the cache.");
            }
        });
        log.info("Delivered quote of the day to {} guild(s). This took {} seconds.", deliveryCount, new DecimalFormat("#.#").format((System.currentTimeMillis() - before) / 1000D));
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
