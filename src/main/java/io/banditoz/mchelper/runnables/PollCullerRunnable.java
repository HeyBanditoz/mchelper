package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.PollService;
import io.banditoz.mchelper.utils.database.Poll;
import io.banditoz.mchelper.utils.database.dao.PollsDao;
import io.banditoz.mchelper.utils.database.dao.PollsDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollCullerRunnable implements Runnable {
    private final PollService pollService;
    private final PollsDao pollsDao;
    private static final Logger log = LoggerFactory.getLogger(PollCullerRunnable.class);

    public PollCullerRunnable(MCHelper mcHelper) {
        this.pollService = mcHelper.getPollService();
        this.pollsDao = new PollsDaoImpl(mcHelper.getDatabase());
    }

    @Override
    public void run() {
        try {
            List<Long> pollsToCull = pollsDao.getPollsNotRespondedToAfter(2, TimeUnit.DAYS).stream().map(Poll::messageId).toList();
            pollService.disablePollsByMessageId(pollsToCull);
            log.info("Culled {} polls. Messages: {}", pollsToCull.size(), pollsToCull);
        } catch (Exception e) {
            log.error("Could not cull polls.", e);
        }
    }
}
