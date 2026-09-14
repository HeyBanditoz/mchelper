package io.banditoz.mchelper.scheduling;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
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

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return new NonBlockingOnCloseScheduledExecutorService(1, new ThreadFactoryBuilder().setNameFormat("Scheduled-%d")
                .build());
    }
}
