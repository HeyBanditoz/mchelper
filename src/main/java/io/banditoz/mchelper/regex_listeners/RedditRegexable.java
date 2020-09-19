package io.banditoz.mchelper.regex_listeners;

import io.banditoz.mchelper.utils.RedditLinkExtractor;

import java.util.regex.Pattern;

public class RedditRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("https://reddit.app.link/\\w.*");

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected void onRegexCommand(RegexCommandEvent re) throws Exception {
        re.sendTyping();
        RedditLinkExtractor rle = new RedditLinkExtractor(re.getMCHelper());
        re.sendReply(rle.extractFromRedditAppLink(re.getArgs()));
    }
}
