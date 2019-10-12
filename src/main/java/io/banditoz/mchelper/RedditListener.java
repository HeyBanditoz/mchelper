package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.CommandUtils;
import io.banditoz.mchelper.utils.ExtractRedditLink;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditListener extends ListenerAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static Pattern pattern = Pattern.compile("https://reddit.app.link/\\w.*");

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Matcher m = pattern.matcher(e.getMessage().getContentDisplay());
        try {
            if (m.find()) {
                CommandUtils.sendReply(ExtractRedditLink.extractFromRedditAppLink(m.group()), e);
            }
        } catch (Exception ex) {
            logger.error("Exception on converting Reddit link!", ex);
        }
    }
}
