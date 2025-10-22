package io.banditoz.mchelper.scheduling;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * An implementation of the {@link ScheduledThreadPoolExecutor} that replaces {@link ExecutorService#close()} with a
 * non-blocking {@link ScheduledThreadPoolExecutor#shutdownNow()} as to not block the shutdown of this bean when the
 * DI container is shut down.
 */
public class NonBlockingOnCloseScheduledExecutorService extends ScheduledThreadPoolExecutor {
    public NonBlockingOnCloseScheduledExecutorService(int corePoolSize) {
        super(corePoolSize);
    }

    public NonBlockingOnCloseScheduledExecutorService(int corePoolSize, @NotNull ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public NonBlockingOnCloseScheduledExecutorService(int corePoolSize, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public NonBlockingOnCloseScheduledExecutorService(int corePoolSize, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    /**
     * Shuts the scheduler down immediately, even if there are scheduled tasks waiting to be executed.
     * Called by Avaje.
     * @apiNote See {@link ScheduledThreadPoolExecutor#shutdownNow()}
     */
    @Override
    public void close() {
        super.shutdownNow();
    }
}
