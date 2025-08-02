package io.banditoz.mchelper;

import io.avaje.inject.RequiresProperty;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO rewrite this to be more extendable for flagged words in the future
 */
@Singleton
@RequiresProperty("TARKOV_CHANNEL")
public class AntiTwitch extends ListenerAdapter {
    private final String tarkovChannel;
    private static final Logger log = LoggerFactory.getLogger(AntiTwitch.class);

    public AntiTwitch() {
        this.tarkovChannel = System.getenv("TARKOV_CHANNEL");
        log.info("Deleting messages from {} which contain twitch.tv links...", tarkovChannel);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && event.getChannel().getId().equals(System.getenv("TARKOV_CHANNEL")) && event.getMessage().getContentRaw().contains("twitch.tv/")) {
            try {
                event.getMessage().delete().reason("This message contained a twitch.tv link from an announcement, and was deleted.").queue(
                        unused -> log.info("Deleted Tarkov message which contained a Twitch.tv link. Message content:\n" + event.getMessage().getContentRaw()),
                        ex -> log.error("Error while sending call to delete message", ex)
                );
            } catch (Exception ex) {
                log.error("Could not delete message", ex);
            }
        }
    }
}
