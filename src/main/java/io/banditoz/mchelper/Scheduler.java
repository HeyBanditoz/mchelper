package io.banditoz.mchelper;

import javax.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.avaje.config.Config;
import io.avaje.inject.PostConstruct;
import io.banditoz.mchelper.runnables.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.LoggerFactory;

@Singleton
public class Scheduler {
    private final ScheduledExecutorService ses;
    private final UserMaintenanceRunnable userMaintenanceRunnable;
    private final PollCullerRunnable pollCullerRunnable;
    private final XonlistCheckerRunnable xonlistCheckerRunnable;
    private final DiscordPingRunnable discordPingRunnable;
    private final QotdRunnable qotdRunnable;

    @Inject
    public Scheduler(ScheduledExecutorService ses,
                     @Nullable UserMaintenanceRunnable userMaintenanceRunnable,
                     @Nullable PollCullerRunnable pollCullerRunnable,
                     @Nullable XonlistCheckerRunnable xonlistCheckerRunnable,
                     @Nullable DiscordPingRunnable discordPingRunnable,
                     QotdRunnable qotdRunnable) {
        this.ses = ses;
        this.userMaintenanceRunnable = userMaintenanceRunnable;
        this.pollCullerRunnable = pollCullerRunnable;
        this.xonlistCheckerRunnable = xonlistCheckerRunnable;
        this.discordPingRunnable = discordPingRunnable;
        this.qotdRunnable = qotdRunnable;
    }

    @PostConstruct
    public void schedule() {
        if (userMaintenanceRunnable != null) {
            ses.scheduleWithFixedDelay(userMaintenanceRunnable,10, 43200, TimeUnit.SECONDS);
        }
        if (pollCullerRunnable != null) {
            ses.scheduleWithFixedDelay(pollCullerRunnable, 120, 86400, TimeUnit.SECONDS);
        }
        if (xonlistCheckerRunnable != null) {
            ses.scheduleWithFixedDelay(xonlistCheckerRunnable, 10, 600, TimeUnit.SECONDS);
        }
        if (discordPingRunnable != null) {
            ses.scheduleWithFixedDelay(discordPingRunnable, 0, Config.getInt("mchelper.metrics.ping-stats.ping-delay-seconds"), TimeUnit.SECONDS);
        }
        ses.scheduleAtFixedRate(qotdRunnable,
                QotdRunnable.getDelay().getSeconds(),
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
        LoggerFactory.getLogger(getClass()).info("Tasks scheduled.");
    }
}
