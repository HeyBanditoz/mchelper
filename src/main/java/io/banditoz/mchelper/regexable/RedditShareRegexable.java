package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.RedditLinkExtractor;
import net.dv8tion.jda.api.Permission;

import java.util.regex.Pattern;

public class RedditShareRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("https://(www.)?reddit.com/r/\\w{3,21}/s/\\w+(?=/$|$)");

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
        RedditLinkExtractor rle = new RedditLinkExtractor(re.getMCHelper());
        String s = rle.extractFromRedditShareLink(re.getArgs());
        re.sendReplyWithoutPingAllowingLinkEmbeds(s.replace("reddit.com/r/", "rxddit.com/r/") + " — [reddit ->](<" + s + ">)");
        if (re.getEvent().getGuild().getSelfMember().hasPermission(re.getEvent().getChannel().asGuildMessageChannel(), Permission.MESSAGE_MANAGE)) {
            re.getEvent().getMessage().suppressEmbeds(true).reason("Reddit link sent, suppressing embeds so the rxddit one can supersede it in another message.").queue();
        }
        return Status.SUCCESS;
    }
}
