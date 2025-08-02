package io.banditoz.mchelper.jda;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OwnerMessenger {
    private final ApplicationInfo applicationInfo;
    private final JDA jda;
    private static final Logger log = LoggerFactory.getLogger(OwnerMessenger.class);

    @Inject
    public OwnerMessenger(ApplicationInfo applicationInfo,
                          JDA jda) {
        this.applicationInfo = applicationInfo;
        this.jda = jda;
    }

    /**
     * Asynchronously messages the current owner of this bot. Messages are not sanitized.
     * Exceptions are logged and not returned to the caller.
     * Intended for debugging.
     *
     * @param message The message to send.
     */
    public void messageOwner(String message) {
        long owner = applicationInfo.getOwner().getIdLong();
        jda.openPrivateChannelById(owner)
                .flatMap(channel -> channel.sendMessage(message))
                .queue(sent -> {}, throwable -> log.error("Could not message owner {}!", owner, throwable));
    }
}
