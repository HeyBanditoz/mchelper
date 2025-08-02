package io.banditoz.mchelper.stats.service;

import java.util.concurrent.ThreadPoolExecutor;

import io.banditoz.mchelper.database.dao.StatisticsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.jda.OwnerMessenger;
import io.banditoz.mchelper.stats.Stat;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
public class DatabaseStatsRecorder implements StatsRecorder {
    private final ThreadPoolExecutor tpe;
    private final StatisticsDao statisticsDao;
    private final OwnerMessenger ownerMessenger;
    private static final Logger log = LoggerFactory.getLogger(DatabaseStatsRecorder.class);

    @Inject
    public DatabaseStatsRecorder(ThreadPoolExecutor tpe,
                                 StatisticsDao statisticsDao,
                                 OwnerMessenger ownerMessenger) {
        this.tpe = tpe;
        this.statisticsDao = statisticsDao;
        this.ownerMessenger = ownerMessenger;
    }

    @Override
    public void record(Stat s) {
        tpe.execute(() -> {
            try {
                statisticsDao.log(s);
            } catch (Exception ex) {
                log.error("Error while recording a Stat!", ex);
                ownerMessenger.messageOwner("Error while recording a Stat! " + ex);
            }
        });
    }
}
