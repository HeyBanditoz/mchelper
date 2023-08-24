package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.ClassUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class RegexableHandler extends ListenerAdapter {
    private final List<Regexable> regexables;
    private final Logger LOGGER = LoggerFactory.getLogger(RegexableHandler.class);
    private final MCHelper MCHELPER;
    private final ConfigurationProvider config;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
        // don't try to run a listener if a command is (probably) present
        if (event.isFromGuild() && event.getMessage().getContentRaw().length() > 0 && config.getValue(Config.PREFIX, event.getGuild().getIdLong()).charAt(0) == event.getMessage().getContentRaw().charAt(0)) return;

        getRegexableByEvent(event).forEach(r -> MCHELPER.getThreadPoolExecutor().execute(() -> {
            if (r.handleCooldown(event.getChannel().getId())) {
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
                        } catch (Exception e) {
                            MCHELPER.getStatsRecorder().record(new LoggableRegexCommandEvent(rce, (int) (System.nanoTime() - before) / 1000000, Status.EXCEPTIONAL_FAILURE));
                            CommandUtils.sendExceptionMessage(event, e, r.LOGGER);
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
            }
        }));
    }

    protected List<Regexable> getRegexableByEvent(MessageReceivedEvent e) {
        return regexables.stream()
                .filter(r -> r.containsRegexable(e.getMessage().getContentRaw())).toList();
    }

    public List<Regexable> getRegexables() {
        return Collections.unmodifiableList(regexables);
    }

    public RegexableHandler(MCHelper mcHelper) throws Exception {
        this.MCHELPER = mcHelper;
        this.config = new ConfigurationProvider(mcHelper);
        regexables = new ArrayList<>();
        LOGGER.info("Registering regexable listeners...");
        long before = System.currentTimeMillis();
        for (Class<? extends Regexable> clazz : ClassUtils.getAllSubtypesOf(Regexable.class)) {
            regexables.add(clazz.getDeclaredConstructor().newInstance());
        }
        LOGGER.info(regexables.size() + " regexable listeners registered in " + (System.currentTimeMillis() - before) + " ms.");
    }
}
