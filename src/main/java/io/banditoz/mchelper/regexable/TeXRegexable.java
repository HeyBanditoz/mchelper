package io.banditoz.mchelper.regexable;

import java.util.regex.Pattern;

import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.TeXRenderer;
import jakarta.inject.Singleton;

@Singleton
public class TeXRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("\\$\\$(.*?)\\$\\$");

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    public Status onRegexCommand(RegexCommandEvent re) throws Exception {
        re.sendTyping();
        TeXRenderer.sendTeXToChannel(re.getEvent(), re.getArgs());
        return Status.SUCCESS;
    }
}
