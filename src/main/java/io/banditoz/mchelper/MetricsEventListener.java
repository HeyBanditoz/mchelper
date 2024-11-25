package io.banditoz.mchelper;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.MeterProvider;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MetricsEventListener extends ListenerAdapter {
    private final LongCounter eventCounter;
    private final LongHistogram discordToBotDelay;
    private static final Logger log = LoggerFactory.getLogger(MetricsEventListener.class);

    public MetricsEventListener(MeterProvider meterProvider) {
        eventCounter = meterProvider
                .meterBuilder("event_metrics")
                .build()
                .counterBuilder("jda_events")
                .setDescription("Counter tracking the number of times each JDA event occurs")
                .build();
        discordToBotDelay = meterProvider
                .meterBuilder("event_metrics")
                .build()
                .histogramBuilder("jda_event_discord_to_bot_delay")
                .setDescription("Histogram tracking the delay of when a snowflake event arrives at the bot versus its creation time within Discord.")
                .ofLongs()
                .setExplicitBucketBoundariesAdvice(List.of(0L, 5L, 10L, 25L, 50L, 75L, 100L, 125L, 150L, 175L, 200L, 225L, 250L, 275L, 300L, 400L, 500L, 750L, 1000L, 2500L, 5000L, 7500L, 10000L))
                .build();
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        long now = System.currentTimeMillis();
        AttributesBuilder attr = Attributes.builder()
                .put("event_name", event.getClass().getSimpleName());
        switch (event) {
            case GenericGuildEvent e ->
                    attr.put("guild", e.getGuild().getIdLong());
            case GenericMessageEvent e when e.isFromGuild() ->
                    attr.put("guild", e.getGuild().getIdLong());
            case Interaction e when e.getGuild() != null ->
                    attr.put("guild", e.getGuild().getIdLong());
            case HttpRequestEvent e ->
                    attr.put("route", "%s /%s".formatted(e.getRoute().getBaseRoute().getMethod(), e.getRoute().getBaseRoute().getRoute()));
            default -> {}
        }

        eventCounter.add(1, attr.build());

        if (event instanceof ISnowflake snowflake) {
            // this long -> OffsetDateTime -> Instant -> long is wasteful...
            long delay = snowflake.getTimeCreated().toInstant().toEpochMilli() - now;
            if (delay < 0) {
                log.warn("Event {} has time traveled with a negative delay of {}", event.getClass().getSimpleName(), delay);
            }
            Attributes attrs = Attributes.builder().put("event_name", event.getClass().getSimpleName()).build();
            discordToBotDelay.record(delay, attrs);
        }
    }
}
