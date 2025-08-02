package io.banditoz.mchelper.jda;

import java.util.concurrent.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.slf4j.LoggerFactory;

@Factory
public class ExecutorFactory {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        int threads = Config.getInt("mchleper.command-threads", 2);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("Command-%d").build());
        LoggerFactory.getLogger(ExecutorFactory.class).info("ThreadPollExecutor built with {} threads.", threads);
        return threadPoolExecutor;
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Scheduled-%d")
                .build());
    }
}
