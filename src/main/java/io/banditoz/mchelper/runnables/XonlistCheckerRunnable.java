package io.banditoz.mchelper.runnables;

import io.avaje.inject.RequiresProperty;
import io.banditoz.mchelper.jda.GuildMessageChannelResolver;
import io.banditoz.mchelper.xonlist.XonlistPlayerNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresProperty("XONLIST_NOTIFICATION_CHANNEL")
public class XonlistCheckerRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(XonlistCheckerRunnable.class);
    private final XonlistPlayerNotifier notifier;
    private final GuildMessageChannelResolver channelResolver;

    @Inject
    public XonlistCheckerRunnable(XonlistPlayerNotifier notifier,
                                  GuildMessageChannelResolver channelResolver) {
        this.notifier = notifier;
        this.channelResolver = channelResolver;
        log.info("Xonlist server checker initialized.");
    }

    @Override
    public void run() {
        try {
            MessageChannel notifChannel = channelResolver.getGuildMessageChannelById(System.getenv("XONLIST_NOTIFICATION_CHANNEL"));
            notifier.checkAndNotify(notifChannel);
        } catch (Exception e) {
            log.error("Could not notify of Xonotic server status.", e);
        }
    }
}
