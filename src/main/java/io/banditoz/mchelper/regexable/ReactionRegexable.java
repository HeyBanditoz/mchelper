package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Status;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ReactionRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
    private static final ArrayList<String> RESPONSES = new ArrayList<>();

    static {
        RESPONSES.add("Liked ");
        RESPONSES.add("Disliked ");
        RESPONSES.add("Emphasized ");
        RESPONSES.add("Loved ");
        RESPONSES.add("Laughed at ");
        RESPONSES.add("Questioned ");
    }

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected Status onRegexCommand(RegexCommandEvent re) throws Exception {
        Random r = ThreadLocalRandom.current();
        if (r.nextDouble() <= 0.1) {
            String reply = RESPONSES.get(r.nextInt(RESPONSES.size())) + "“" + re.getEvent().getMessage().getContentRaw() + "”";
            re.getEvent().getChannel().sendMessage(reply).queueAfter(3, TimeUnit.SECONDS);
            return Status.SUCCESS;
        }
        else {
            return Status.FAIL;
        }
    }

}
