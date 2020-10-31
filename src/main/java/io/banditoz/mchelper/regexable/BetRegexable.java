package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Status;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class BetRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("\\bbet((s)?|(ting)?|(ted))\\b", Pattern.CASE_INSENSITIVE);
    private static final String[] BETS = {"Bet!", "Bet?", "Bet.", "How much you wanna bet?", "Okay, bet!"};

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
        re.sendReply(BETS[ThreadLocalRandom.current().nextInt(BETS.length)]);
        return Status.SUCCESS;
    }
}
