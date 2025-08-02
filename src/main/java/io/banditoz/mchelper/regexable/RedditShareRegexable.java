package io.banditoz.mchelper.regexable;

import java.util.regex.Pattern;

import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.RedditLinkExtractor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.Permission;

@Singleton
public class RedditShareRegexable extends Regexable {
    private final RedditLinkExtractor redditLinkExtractor;
    private static final Pattern PATTERN = Pattern.compile("https://(www.)?reddit.com/r/\\w{3,21}/s/\\w+(?=/$|$)");

    @Inject
    public RedditShareRegexable(RedditLinkExtractor redditLinkExtractor) {
        this.redditLinkExtractor = redditLinkExtractor;
    }

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected Status onRegexCommand(RegexCommandEvent re) throws Exception {
        if (!re.getConfig().get(Config.BETTER_REDDIT_LINKS).equals("true")) {
            return Status.NOT_CONFIGURED;
        }
        re.sendTyping();
        String s = redditLinkExtractor.extractFromRedditShareLink(re.getArgs());
        re.sendReplyWithoutPingAllowingLinkEmbeds(s.replace("reddit.com/r/", "rxddit.com/r/") + " — [reddit ->](<" + s + ">)");
        if (re.getEvent().getGuild().getSelfMember().hasPermission(re.getEvent().getChannel().asGuildMessageChannel(), Permission.MESSAGE_MANAGE)) {
            re.getEvent().getMessage().suppressEmbeds(true).reason("Reddit link sent, suppressing embeds so the rxddit one can supersede it in another message.").queue();
        }
        return Status.SUCCESS;
    }
}
