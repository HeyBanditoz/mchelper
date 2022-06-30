package io.banditoz.mchelper.stats;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.database.dao.StatisticsDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public class StatsRecorder {
    private final MCHelper MCHELPER;
    private final ExecutorService ES;
    private final Logger LOGGER = LoggerFactory.getLogger(StatsRecorder.class);

    public StatsRecorder(MCHelper mcHelper, ExecutorService es) {
        this.MCHELPER = mcHelper;
        this.ES = es;
    }

    public void record(Stat s) {
        if (!MCHELPER.getSettings().getRecordCommandAndRegexStatistics() || MCHELPER.getDatabase() == null) {
            return;
        }
        ES.execute(() -> {
            try {
                new StatisticsDaoImpl(MCHELPER.getDatabase()).log(s);
            } catch (Exception ex) {
                LOGGER.error("Error while recording a Stat!", ex);
                MCHELPER.messageOwner("Error while recording a Stat! " + ex);
            }
        });
    }
}
