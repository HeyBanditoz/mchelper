package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ExtractRedditLink;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditListener extends ListenerAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static Pattern pattern = Pattern.compile("https://reddit.app.link/\\w.*");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Matcher m = pattern.matcher(event.getMessage().getContentDisplay());
        try {
            if (m.find()) {
                event.getChannel().sendMessage(ExtractRedditLink.extractFromRedditAppLink(m.group())).queue();
            }
        } catch (Exception ex) {
            logger.error("Exception on converting Reddit link!", ex);
        }
    }
}