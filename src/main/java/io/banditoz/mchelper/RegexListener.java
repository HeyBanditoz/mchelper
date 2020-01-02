package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.utils.SettingsManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RegexListener extends ListenerAdapter {
    protected abstract void onMessage(RegexEvent re);
    protected abstract String regex();
    private MessageReceivedEvent e;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected static final ExecutorService ES = Executors.newFixedThreadPool(SettingsManager.getInstance().getSettings().getRegexListenerThreads());

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        initialize(e);
        ES.execute(() -> {
            try {
                long before = System.nanoTime();
                onMessage(new RegexEvent(e, LOGGER, regex()));
                long after = System.nanoTime() - before;
                LOGGER.debug("Listener ran in " + (after / 1000000) + " ms.");
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(this.e, ex, LOGGER, false, false);
            }});
            LOGGER.debug(ES.toString());
    }

    private void initialize(MessageReceivedEvent e) {
        this.e = e;
    }
}
