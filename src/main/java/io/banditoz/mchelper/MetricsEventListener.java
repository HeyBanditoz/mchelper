package io.banditoz.mchelper;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MetricsEventListener extends ListenerAdapter {
    private final LongCounter eventCounter;

    public MetricsEventListener(MeterProvider meterProvider) {
        eventCounter = meterProvider
                .meterBuilder("event_metrics")
                .build()
                .counterBuilder("jda_events")
                .setDescription("Counter tracking the number of times each JDA event occurs")
                .build();
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        AttributesBuilder attr = Attributes.builder()
                .put("event_name", event.getClass().getSimpleName());
        if (event instanceof GenericGuildEvent e) {
            attr.put("guild", e.getGuild().getIdLong());
        }
        else if (event instanceof GenericMessageEvent e && e.isFromGuild()) {
            attr.put("guild", e.getGuild().getIdLong());
        }

        eventCounter.add(1, attr.build());
    }
}
