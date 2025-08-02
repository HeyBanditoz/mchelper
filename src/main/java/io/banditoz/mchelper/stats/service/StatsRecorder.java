package io.banditoz.mchelper.stats.service;

import io.banditoz.mchelper.stats.Stat;

public interface StatsRecorder {
    void record(Stat s);
}
