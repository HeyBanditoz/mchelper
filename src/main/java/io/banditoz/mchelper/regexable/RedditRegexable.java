package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.RedditLinkExtractor;

import java.util.regex.Pattern;

public class RedditRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("https://reddit.app.link/\\w.*");

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected Status onRegexCommand(RegexCommandEvent re) throws Exception {
        re.sendTyping();
        RedditLinkExtractor rle = new RedditLinkExtractor(re.getMCHelper());
        re.sendReply(rle.extractFromRedditAppLink(re.getArgs()));
        return Status.SUCCESS;
    }
}
