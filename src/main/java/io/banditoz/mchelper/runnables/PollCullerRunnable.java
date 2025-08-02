package io.banditoz.mchelper.runnables;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

import io.banditoz.mchelper.PollService;
import io.banditoz.mchelper.database.Poll;
import io.banditoz.mchelper.database.dao.PollsDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase
public class PollCullerRunnable implements Runnable {
    private final PollService pollService;
    private final PollsDao pollsDao;
    private final JDA jda;
    private static final Logger log = LoggerFactory.getLogger(PollCullerRunnable.class);

    @Inject
    public PollCullerRunnable(PollService pollService,
                              PollsDao pollsDao,
                              JDA jda) {
        this.pollService = pollService;
        this.pollsDao = pollsDao;
        this.jda = jda;
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
