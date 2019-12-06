package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexListener extends ListenerAdapter {
    protected abstract void onMessage(CommandEvent ce);
    protected abstract String regex();
    protected MessageReceivedEvent e;
    protected String message;
    protected Matcher m;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected static final ExecutorService ES = Executors.newFixedThreadPool(2);

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        initialize(e);
        ES.execute(() -> {
            try {
                long before = System.nanoTime();
                onMessage(new CommandEvent(e, LOGGER));
                long after = System.nanoTime() - before;
                LOGGER.debug("Listener ran in " + (after / 1000000) + " ms.");
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(this.e, ex, LOGGER, false, false);
            }});
            LOGGER.debug(ES.toString());
    }

    private void initialize(MessageReceivedEvent e) {
        this.e = e;
        this.message = this.e.getMessage().getContentDisplay();
        Pattern p = Pattern.compile(regex(), Pattern.DOTALL);
        m = p.matcher(message);
    }
}
