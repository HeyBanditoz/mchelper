package io.banditoz.mchelper;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
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
        switch (event) {
            case GenericGuildEvent e ->
                    attr.put("guild", e.getGuild().getIdLong());
            case GenericMessageEvent e when e.isFromGuild() ->
                    attr.put("guild", e.getGuild().getIdLong());
            case Interaction e when e.getGuild() != null ->
                    attr.put("guild", e.getGuild().getIdLong());
            default -> {}
        }

        eventCounter.add(1, attr.build());
    }
}
