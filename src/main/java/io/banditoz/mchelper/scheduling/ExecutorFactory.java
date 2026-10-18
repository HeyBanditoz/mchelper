package io.banditoz.mchelper.scheduling;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Secondary;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

@Factory
public class ExecutorFactory {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        int threads = Config.getInt("mchelper.command-threads", 2);
        ThreadPoolExecutor threadPoolExecutor = new ContextPropagatingThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("Command-%d").build());
        LoggerFactory.getLogger(ExecutorFactory.class).info("ThreadPollExecutor built with {} threads.", threads);
        return threadPoolExecutor;
    }

    /**
     * Autocomplete replies can't be deferred and Discord discards them after three seconds, so they get their own
     * threads rather than queueing behind whatever slow command is occupying the (small) shared command pool.
     * Secondary so that injecting a plain {@link ThreadPoolExecutor} still gets the command pool.
     */
    @Bean
    @Secondary
    @Named("autocomplete")
    public ThreadPoolExecutor autoCompleteExecutor() {
        return new ContextPropagatingThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("Autocomplete-%d").build());
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return new NonBlockingOnCloseScheduledExecutorService(2, new ThreadFactoryBuilder().setNameFormat("Scheduled-%d")
                .build());
    }
}
