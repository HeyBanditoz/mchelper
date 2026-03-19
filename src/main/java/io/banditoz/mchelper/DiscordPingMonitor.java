package io.banditoz.mchelper;

import java.util.List;

import io.avaje.inject.RequiresProperty;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.MeterProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;

@Singleton
@RequiresProperty("mchelper.metrics.ping-stats.enabled")
public class DiscordPingMonitor {
    private final JDA jda;
    private final LongHistogram restDelay;
    private final LongHistogram gatewayDelay;

    @Inject
    public DiscordPingMonitor(JDA jda,
                              MeterProvider meterProvider) {
        this.jda = jda;
        this.restDelay = meterProvider
                .meterBuilder("ping_metrics")
                .build()
                .histogramBuilder("mchelper_ping_delay_rest_milliseconds")
                .setDescription("Counter tracking REST delay in milliseconds, measured by calling the users API.")
                .ofLongs()
                .setExplicitBucketBoundariesAdvice(List.of(0L, 5L, 10L, 25L, 50L, 75L, 100L, 125L, 150L, 175L, 200L, 225L, 250L, 275L, 300L, 400L, 500L, 750L, 1000L))
                .build();
        this.gatewayDelay = meterProvider
                .meterBuilder("ping_metrics")
                .build()
                .histogramBuilder("mchelper_ping_delay_gateway_milliseconds")
                .setDescription("Counter tracking gateway delay in milliseconds.")
                .ofLongs()
                .setExplicitBucketBoundariesAdvice(List.of(0L, 5L, 10L, 25L, 50L, 75L, 100L, 125L, 150L, 175L, 200L, 225L, 250L, 275L, 300L, 400L, 500L, 750L, 1000L))
                .build();
    }

    public void ping() {
        jda.getRestPing().queue(gatewayDelay::record);
        restDelay.record(jda.getGatewayPing());
    }
}
