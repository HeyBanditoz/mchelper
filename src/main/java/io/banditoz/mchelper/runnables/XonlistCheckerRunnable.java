package io.banditoz.mchelper.runnables;

import io.avaje.inject.RequiresProperty;
import io.banditoz.mchelper.xonlist.XonlistPlayerNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresProperty("XONLIST_NOTIFICATION_CHANNEL")
public class XonlistCheckerRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(XonlistCheckerRunnable.class);
    private final XonlistPlayerNotifier notifier;
    private final JDA jda;

    @Inject
    public XonlistCheckerRunnable(XonlistPlayerNotifier notifier,
                                  JDA jda) {
        this.notifier = notifier;
        this.jda = jda;
        log.info("Xonlist server checker initialized.");
    }

    @Override
    public void run() {
        try {
            TextChannel notifChannel = jda.getTextChannelById(System.getenv("XONLIST_NOTIFICATION_CHANNEL"));
            notifier.checkAndNotify(notifChannel);
        } catch (Exception e) {
            log.error("Could not notify of Xonotic server status.", e);
        }
    }
}
