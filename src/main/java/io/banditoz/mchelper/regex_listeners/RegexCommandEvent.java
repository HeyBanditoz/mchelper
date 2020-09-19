package io.banditoz.mchelper.regex_listeners;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class RegexCommandEvent {
    private final MessageReceivedEvent EVENT;
    private final MCHelper MCHELPER;
    private final Logger LOGGER;
    private final String ARGS;

    public RegexCommandEvent(@NotNull MessageReceivedEvent event, MCHelper mcHelper, String args, Logger logger) {
        this.EVENT = event;
        this.MCHELPER = mcHelper;
        this.ARGS = args;
        this.LOGGER = logger;
    }

    public MessageReceivedEvent getEvent() {
        return EVENT;
    }

    public String getArgs() {
        return ARGS;
    }

    public MCHelper getMCHelper() {
        return MCHELPER;
    }

    /**
     * Sends a reply. All mentions will be sanitized.
     *
     * @param msg The reply.
     */
    public void sendReply(String msg) {
        CommandUtils.sendReply(msg, EVENT);
    }

    /**
     * Sends typing to where this event came from.
     */
    public void sendTyping() {
        getEvent().getChannel().sendTyping().queue();
    }
}
