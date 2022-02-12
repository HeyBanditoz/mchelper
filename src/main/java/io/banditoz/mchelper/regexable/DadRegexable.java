package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DadRegexable extends Regexable {
    private static final Pattern PATTERN = Pattern.compile("^(i['’]?)m(ma)?\\b", Pattern.CASE_INSENSITIVE);

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
        GuildConfigDao dao = new GuildConfigDaoImpl(re.getMCHelper().getDatabase());
        GuildConfig gc = dao.getConfig(re.getEvent().getGuild());
        if (ThreadLocalRandom.current().nextDouble() <= gc.getDadBotChance()) {
            Matcher m = PATTERN.matcher(re.getEvent().getMessage().getContentRaw());
            re.sendReplyWithoutPing("Hi" + m.replaceFirst("") + ", I'm Dad!");
        }
        return Status.SUCCESS;
    }
}
