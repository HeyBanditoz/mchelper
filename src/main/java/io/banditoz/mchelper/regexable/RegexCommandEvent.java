package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.UserEvent;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.config.GuildConfigurationProvider;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.LocalDateTime;

public class RegexCommandEvent implements UserEvent {
    protected final MessageReceivedEvent EVENT;
    private final MCHelper MCHELPER;
    private final Logger LOGGER;
    protected final String ARGS;
    protected final String CLASS_NAME;
    protected final LocalDateTime EXECUTED_WHEN = LocalDateTime.now(); // hopefully accurate within a second
    protected final GuildConfigurationProvider CONFIG;

    public RegexCommandEvent(@NotNull MessageReceivedEvent event, MCHelper mcHelper, String args, Logger logger, String className) {
        this.EVENT = event;
        this.MCHELPER = mcHelper;
        this.ARGS = args;
        this.LOGGER = logger;
        this.CLASS_NAME = className;
        this.CONFIG = new GuildConfigurationProvider(this);
    }

    public MessageReceivedEvent getEvent() {
        return EVENT;
    }

    public String getClassName() {
        return CLASS_NAME;
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
     * Sends a reply. All mentions will be sanitized. The invoking user will <i>not</i> be pinged.
     *
     * @param msg The reply.
     */
    public void sendReplyWithoutPing(String msg) {
        CommandUtils.sendReplyWithoutPing(msg, EVENT);
    }

    public void sendReplyWithoutPingAllowingLinkEmbeds(String msg) {
        CommandUtils.sendReplyWithoutPing(msg, EVENT, true);
    }

    /**
     * Sends typing to where this event came from.
     */
    public void sendTyping() {
        getEvent().getChannel().sendTyping().queue(unused -> {}, throwable -> {}); // silence sendTyping errors when Discord shuts that endpoint off
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public LocalDateTime getExecutedWhen() {
        return EXECUTED_WHEN;
    }

    @Override
    public Guild getGuild() {
        return EVENT.getGuild();
    }

    @Override
    public Database getDatabase() {
        return MCHELPER.getDatabase();
    }

    @Override
    public GuildConfigurationProvider getConfig() {
        return CONFIG;
    }

    @Override
    public User getUser() {
        return EVENT.getAuthor();
    }

    @Override
    public String commandName() {
        return CLASS_NAME;
    }

    @Override
    public long getUserId() {
        return EVENT.getAuthor().getIdLong();
    }
}
