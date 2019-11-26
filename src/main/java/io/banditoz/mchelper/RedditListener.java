package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.RedditLinkExtractor;

public class RedditListener extends RegexListener {
    @Override
    protected String regex() {
        return "https://reddit.app.link/\\w.*";
    }

    @Override
    protected void onMessage() {
        try {
            if (m.find()) {
                e.getChannel().sendTyping().queue();
                sendReply(RedditLinkExtractor.extractFromRedditAppLink(m.group()));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception on converting Reddit link!", ex);
        }
    }
}
