package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.PollService;
import io.banditoz.mchelper.utils.database.Poll;
import io.banditoz.mchelper.utils.database.dao.PollsDao;
import io.banditoz.mchelper.utils.database.dao.PollsDaoImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

public class PollCullerRunnable implements Runnable {
    private final PollService pollService;
    private final PollsDao pollsDao;
    private final JDA jda;
    private static final Logger log = LoggerFactory.getLogger(PollCullerRunnable.class);

    public PollCullerRunnable(MCHelper mcHelper) {
        this.pollService = mcHelper.getPollService();
        this.pollsDao = new PollsDaoImpl(mcHelper.getDatabase());
        this.jda = mcHelper.getJDA();
    }

    @Override
    public void run() {
        try {
            List<Poll> pollsToCull = pollsDao.getPollsNotRespondedToAfter(2, TimeUnit.DAYS);
            if (pollsToCull.isEmpty()) {
                return;
            }
            pollService.disablePollsByMessageId(pollsToCull.stream().map(Poll::messageId).toList());
            for (Poll p : pollsToCull) {
                try {
                    jda.getChannelById(TextChannel.class, p.channelId())
                            .retrieveMessageById(p.messageId())
                            .flatMap(m -> m.editMessageComponents(emptyList()))
                            .queue();
                } catch (Exception ex) {
                    log.warn("Could not remove buttons from poll " + p, ex);
                }
            }
            log.info("Culled {} polls. Messages: {}", pollsToCull.size(), pollsToCull);
        } catch (Exception e) {
            log.error("Could not cull polls.", e);
        }
    }
}
