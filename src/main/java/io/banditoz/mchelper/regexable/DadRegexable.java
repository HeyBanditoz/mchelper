package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DadRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("^(i'?m)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected int getCooldown() {
        return 5;
    }

    @Override
    protected Status onRegexCommand(RegexCommandEvent re) throws Exception {
        Matcher m = PATTERN.matcher(re.getEvent().getMessage().getContentRaw());
        re.sendReply("Hi" + m.replaceFirst("") + ", I'm Dad!");
        return Status.SUCCESS;
    }
}
