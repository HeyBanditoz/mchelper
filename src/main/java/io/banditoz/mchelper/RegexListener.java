package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.CommandUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexListener extends ListenerAdapter {
    protected abstract void onMessage();
    protected abstract String regex();
    protected MessageReceivedEvent e;
    protected Logger logger;
    protected String message;
    protected Matcher m;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        initialize(e);
        try {
            Thread thread = new Thread(() -> {
                long before = System.nanoTime();
                onMessage();
                long after = System.nanoTime() - before;
                logger.debug("Listener ran in " + (after / 1000000) + " ms.");
            });
            thread.start();
        } catch (Exception ex) {
            CommandUtils.sendExceptionMessage(this.e, ex, logger, false, false);
        }
    }

    private void initialize(MessageReceivedEvent e) {
        this.e = e;
        this.message = this.e.getMessage().getContentDisplay();
        Pattern p = Pattern.compile(regex());
        m = p.matcher(message);
        logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Sends a reply.
     * @param msg The reply.
     */
    public void sendReply(String msg) {
        CommandUtils.sendReply(msg, e);
    }

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex) {
        CommandUtils.sendExceptionMessage(this.e, ex, logger, true, false);
    }
}
