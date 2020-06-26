package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.CommandUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RegexListener extends ListenerAdapter {
    protected abstract void onMessage(RegexEvent re);
    protected abstract String regex();
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final MCHelper MCHELPER;

    public RegexListener(MCHelper mcHelper) {
        this.MCHELPER = mcHelper; // TODO rewrite it so it's like CommandHandler
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getAuthor().getIdLong() == e.getJDA().getSelfUser().getIdLong()) return; // don't run on own messages
        MCHELPER.getThreadPoolExecutor().execute(() -> {
            try {
                long before = System.nanoTime();
                onMessage(new RegexEvent(e, LOGGER, regex(), MCHELPER));
                long after = System.nanoTime() - before;
                LOGGER.debug("Listener ran in " + (after / 1000000) + " ms.");
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(e, ex, LOGGER, false, false);
            }
        });
    }
}
