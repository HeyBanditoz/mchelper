package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.RedditLinkExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditListener extends Listener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static Pattern pattern = Pattern.compile("https://reddit.app.link/\\w.*");

    @Override
    protected void onMessage() {
        Matcher m = pattern.matcher(e.getMessage().getContentDisplay());
        try {
            if (m.find()) {
                e.getChannel().sendTyping().queue();
                sendReply(RedditLinkExtractor.extractFromRedditAppLink(m.group()));
            }
        } catch (Exception ex) {
            logger.error("Exception on converting Reddit link!", ex);
        }
    }
}
