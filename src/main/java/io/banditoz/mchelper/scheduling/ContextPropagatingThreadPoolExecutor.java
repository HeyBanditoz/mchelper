package io.banditoz.mchelper.scheduling;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.context.Context;

public class ContextPropagatingThreadPoolExecutor extends ThreadPoolExecutor {
    public ContextPropagatingThreadPoolExecutor(int corePoolSize,
                                                int maximumPoolSize,
                                                long keepAliveTime,
                                                TimeUnit unit,
                                                BlockingQueue<Runnable> workQueue,
                                                ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(Context.current().wrap(command));
    }
}
