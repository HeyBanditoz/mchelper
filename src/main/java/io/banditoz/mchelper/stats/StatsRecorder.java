package io.banditoz.mchelper.stats;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.dao.StatisticsDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StatsRecorder {
    private final MCHelper MCHELPER;
    private final ThreadPoolExecutor TPE;
    private final Logger LOGGER = LoggerFactory.getLogger(StatsRecorder.class);

    public StatsRecorder(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
        TPE = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("StatsRecorder-%d").build());
    }

    public void record(Stat s) {
        if (!MCHELPER.getSettings().getRecordCommandAndRegexStatistics() || MCHELPER.getDatabase() == null) {
            return;
        }
        TPE.execute(() -> {
            try {
                new StatisticsDaoImpl(MCHELPER.getDatabase()).log(s);
            } catch (Exception ex) {
                LOGGER.error("Error while recording a Stat!", ex);
            }
        });
    }
}
