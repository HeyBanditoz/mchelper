package io.banditoz.mchelper.stats.service;

import io.avaje.inject.Secondary;
import io.banditoz.mchelper.stats.Stat;
import jakarta.inject.Singleton;

@Singleton
@Secondary
public class NoopStatsRecorder implements StatsRecorder {
    @Override
    public void record(Stat s) {
        // no-op
    }
}
