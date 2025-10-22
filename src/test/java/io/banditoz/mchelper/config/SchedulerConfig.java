package io.banditoz.mchelper.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;
import io.banditoz.mchelper.Scheduler;

import static org.mockito.Mockito.mock;

@TestScope
@Factory
public class SchedulerConfig {
    @Bean
    public Scheduler disableScheduler() {
        return mock(Scheduler.class);
    }
}
