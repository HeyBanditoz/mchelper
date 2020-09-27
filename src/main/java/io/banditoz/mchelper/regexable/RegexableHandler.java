package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class RegexableHandler extends ListenerAdapter {
    private final List<Regexable> regexables;
    private final Logger LOGGER = LoggerFactory.getLogger(RegexableHandler.class);
    private final MCHelper MCHELPER;
    private final GuildConfigDao dao;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        // don't try to run a listener if a command is (probably) present
        if (event.getMessage().getContentRaw().length() > 0 && dao.getConfig(event.getGuild()).getPrefix() == event.getMessage().getContentRaw().charAt(0)) return;

        getRegexableByEvent(event).ifPresent(r -> MCHELPER.getThreadPoolExecutor().execute(() -> {
            long before = System.nanoTime();
            try {
                Matcher m = r.regex().matcher(event.getMessage().getContentRaw());
                // even though this would have previously passed in the Stream's filter, (r.containsRegexable()),
                // it would amaze me if this if statement fails. there must be terribly something wrong with the
                // world (or JVM, but it could be both!) if it does. I should rewrite it to only try to match once,
                // instead of twice.
                if (m.find()) {
                    // regex matched... let's continue.
                    RegexCommandEvent rce = new RegexCommandEvent(event, MCHELPER, m.group(), r.LOGGER, r.getClass().getSimpleName());
                    try {
                        Status status = r.onRegexCommand(rce);
                        Stat s = new LoggableRegexCommandEvent(rce, (int) (System.nanoTime() - before) / 1000000, status);
                        MCHELPER.getStatsRecorder().record(s);
                        LOGGER.info(s.getLogMessage());
                    }
                    catch (Exception e) {
                        MCHELPER.getStatsRecorder().record(new LoggableRegexCommandEvent(rce, (int) (System.nanoTime() - before) / 1000000, Status.EXCEPTIONAL_FAILURE));
                    } catch (Throwable t) {
                        if (t instanceof OutOfMemoryError) {
                            System.gc();
                        }
                        throw t; // rethrow
                    }
                }
                else {
                    LOGGER.warn(r.getClass().getName() + " somehow passed initial filtering but now does not match! What the hell is happening?!");
                }
            } catch (Exception e) {
                r.LOGGER.error("Regex processing threw exception!", e);
            }
        }));
    }

    protected Optional<Regexable> getRegexableByEvent(MessageReceivedEvent e) {
        return regexables.stream()
                .filter(r -> r.containsRegexable(e.getMessage().getContentRaw()))
                .findAny();
    }

    public RegexableHandler(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
        this.dao = new GuildConfigDaoImpl(mcHelper.getDatabase());
        regexables = new ArrayList<>();
        regexables.add(new TeXRegexable());
        regexables.add(new RedditRegexable());
        LOGGER.info(regexables.size() + " regexable listeners registered.");
    }
}
