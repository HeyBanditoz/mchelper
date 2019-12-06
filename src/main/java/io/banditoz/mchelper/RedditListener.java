package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.RedditLinkExtractor;

public class RedditListener extends RegexListener {
    @Override
    protected String regex() {
        return "https://reddit.app.link/\\w.*";
    }

    @Override
    protected void onMessage(RegexEvent re) {
        try {
            if (re.getMatcher().find()) {
                re.getEvent().getChannel().sendTyping().queue();
                re.sendReply(RedditLinkExtractor.extractFromRedditAppLink(re.getMatcher().group()));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception on converting Reddit link!", ex);
        }
    }
}
