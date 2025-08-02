package io.banditoz.mchelper.regexable;

import java.util.regex.Pattern;

import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.RedditLinkExtractor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RedditRegexable extends Regexable {
    private final RedditLinkExtractor redditLinkExtractor;
    private static final Pattern PATTERN = Pattern.compile("https://reddit.app.link/\\w.*");

    @Inject
    public RedditRegexable(RedditLinkExtractor redditLinkExtractor) {
        this.redditLinkExtractor = redditLinkExtractor;
    }

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected Status onRegexCommand(RegexCommandEvent re) throws Exception {
        re.sendTyping();
        re.sendReply(redditLinkExtractor.extractFromRedditAppLink(re.getArgs()));
        return Status.SUCCESS;
    }
}
