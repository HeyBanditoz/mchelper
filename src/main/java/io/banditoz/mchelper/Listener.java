package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.CommandUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Listener extends ListenerAdapter {
    protected abstract void onMessage();
    protected MessageReceivedEvent e;
    protected Logger logger;
    protected String message;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        this.e = e;
        this.message = e.getMessage().getContentDisplay();
        logger = LoggerFactory.getLogger(getClass());
        try {
            onMessage();
        } catch (Exception ex) {
            CommandUtils.sendExceptionMessage(this.e, ex, logger, false, false);
        }
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
