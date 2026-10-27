package io.banditoz.mchelper.jda;

import java.util.Set;
import java.util.stream.Collectors;

import io.avaje.config.Config;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IgnoredChannels {
    private static final String KEY = "mchelper.ignore.channel-ids";
    private final Set<Long> channelIds;
    private static final Logger log = LoggerFactory.getLogger(IgnoredChannels.class);

    public IgnoredChannels() {
        this.channelIds = Config.list().of(KEY).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(IgnoredChannels::parse)
                .collect(Collectors.toUnmodifiableSet());
        if (!channelIds.isEmpty()) {
            log.info("Ignoring {} channel(s): {}", channelIds.size(), channelIds);
        }
    }

    public boolean isIgnored(Channel channel) {
        return channelIds.contains(channel.getIdLong());
    }

    private static long parse(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid channel ID \"" + id + "\" in " + KEY, e);
        }
    }
}
