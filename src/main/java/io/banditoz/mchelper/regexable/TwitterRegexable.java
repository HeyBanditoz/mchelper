package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.stats.Status;
import net.dv8tion.jda.api.Permission;

import java.util.regex.Pattern;

public class TwitterRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("https://(www.)?twitter.com/\\w{1,45}/status/\\d+");

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected Status onRegexCommand(RegexCommandEvent re) throws Exception {
        if (!re.getConfig().get(Config.BETTER_TWITTER_LINKS).equals("true")) {
            return Status.NOT_CONFIGURED;
        }
        re.sendTyping();
        String twitterLink = re.getArgs().split("\\?")[0];
        re.sendReplyWithoutPingAllowingLinkEmbeds(
                twitterLink.replaceFirst("twitter.com", "vxtwitter.com")
                        + "\n<"
                        + twitterLink.replaceFirst("twitter.com", "nitter.net")
                        + '>'
        );
        if (re.getEvent().getGuild().getSelfMember().hasPermission(re.getEvent().getChannel().asGuildMessageChannel(), Permission.MESSAGE_MANAGE)) {
            re.getEvent().getMessage().suppressEmbeds(true).reason("Twitter link sent, suppressing embeds so the vxtwitter one can supersede it in another message.").queue();
        }
        return Status.SUCCESS;
    }
}
