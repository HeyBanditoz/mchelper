package io.banditoz.mchelper.runnables;

import io.avaje.inject.RequiresProperty;
import io.banditoz.mchelper.DiscordPingMonitor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresProperty(value = "mchelper.metrics.ping-stats.enabled", equalTo = "true")
public class DiscordPingRunnable implements Runnable {
    private final DiscordPingMonitor discordPingMonitor;

    @Inject
    public DiscordPingRunnable(DiscordPingMonitor discordPingMonitor) {
        this.discordPingMonitor = discordPingMonitor;
    }

    @Override
    public void run() {
        discordPingMonitor.ping();
    }
}
