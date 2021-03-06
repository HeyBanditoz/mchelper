package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.paste.Paste;
import io.banditoz.mchelper.utils.paste.PasteggUploader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;

/**
 * Class which holds the MessageReceivedEvent and command arguments.
 */
public class CommandEvent {
    private String COMMAND_ARGS_STRING;
    private String[] COMMAND_ARGS;
    private String[] RAW_ARGS;
    private final MessageReceivedEvent EVENT;
    private final Logger LOGGER;
    private final Guild GUILD;
    private final MCHelper MCHELPER;
    private final boolean IS_ELEVATED;
    private final Database DATABASE;
    private final String COMMAND_NAME;
    private final LocalDateTime EXECUTED_WHEN = LocalDateTime.now(); // hopefully accurate within a second

    public CommandEvent(@NotNull MessageReceivedEvent event, Logger logger, MCHelper mcHelper, String commandClassName) {
        this.EVENT = event;
        this.LOGGER = logger;
        this.GUILD = (event.isFromGuild()) ? event.getGuild() : null;
        this.IS_ELEVATED = CommandPermissions.isBotOwner(event.getAuthor(), mcHelper.getSettings());
        this.MCHELPER = mcHelper;
        this.DATABASE = mcHelper.getDatabase();
        this.COMMAND_NAME = commandClassName;
    }

    /**
     * Grabs the command arguments formatted as a String.
     *
     * @return The arguments.
     */
    public String getCommandArgsString() {
        if (COMMAND_ARGS_STRING == null) {
            COMMAND_ARGS_STRING = CommandUtils.generateCommandArgsString(EVENT);
        }
        return COMMAND_ARGS_STRING;
    }

    /**
     * Grabs the command arguments as an array.
     *
     * @return The arguments.
     */
    public String[] getCommandArgs() {
        if (COMMAND_ARGS == null) {
            COMMAND_ARGS = CommandUtils.commandArgs(EVENT.getMessage().getContentDisplay());
        }
        return COMMAND_ARGS;
    }

    /**
     * Grabs the <i>raw</i> command arguments as an array.
     *
     * @return The arguments.
     */
    public String[] getRawCommandArgs() {
        if (RAW_ARGS == null) {
            RAW_ARGS = CommandUtils.commandArgs(EVENT.getMessage().getContentRaw());
        }
        return RAW_ARGS;
    }

    /**
     * Grabs the command arguments, without the first element (prefix and command name)
     *
     * @return The arguments.
     */
    public String[] getCommandArgsWithoutName() {
        if (COMMAND_ARGS == null) {
            COMMAND_ARGS = CommandUtils.commandArgs(EVENT.getMessage().getContentDisplay());
        }
        // we don't hold this on the class level because you probably shouldn't be running this more than once (for argparse4j)
        String[] newArgs = new String[COMMAND_ARGS.length - 1];
        System.arraycopy(COMMAND_ARGS, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }

    /**
     * Grabs the MessageReceivedEvent.
     *
     * @return the MessageReceivedEvent.
     */
    public MessageReceivedEvent getEvent() {
        return EVENT;
    }

    /**
     * Gets the guild. This could be null if the command didn't come from a guild.
     *
     * @return The Guild, or null if the command didn't happen in one.
     */
    public Guild getGuild() {
        return GUILD;
    }

    /**
     * Sends a reply containing the exception message.
     *
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex) {
        CommandUtils.sendExceptionMessage(this.EVENT, ex, LOGGER);
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
     * Sends a reply. If it is over 2000 characters, it will be placed into paste.gg. Otherwise. the reply is sent
     * sanitized.
     *
     * @param msg The reply.
     */
    public void sendPastableReply(String msg) {
        if (msg.length() > 2000) {
            try {
                sendReply(new PasteggUploader(this.MCHELPER).uploadToPastegg(new Paste(msg)));
            } catch (Exception e) {
                sendReply(msg);
            }
        }
        else {
            sendReply(msg);
        }
    }

    /**
     * Sends a reply. All mentions will <b>NOT</b> be sanitized.
     *
     * @param msg The reply.
     */
    public void sendUnsanitizedReply(String msg) {
        CommandUtils.sendUnsanitizedReply(msg, EVENT);
    }

    /**
     * Sends an EmbedReply.
     *
     * @param me The reply.
     */
    public void sendEmbedReply(MessageEmbed me) {
        EVENT.getChannel().sendMessageEmbeds(me).queue();
    }

    public void sendImageReply(String msg, ByteArrayOutputStream image) throws Exception {
        CommandUtils.sendImageReply(msg, image, this.EVENT, true);
    }

    public void sendEmbedImageReply(MessageEmbed me, ByteArrayOutputStream image) throws Exception {
        CommandUtils.sendEmbedImageReply(me, image, this.EVENT);
    }

    public void sendEmbedThumbnailReply(MessageEmbed me, ByteArrayOutputStream image, String randomUUID) throws Exception {
        CommandUtils.sendEmbedThumbnailReply(me, image, randomUUID, this.EVENT);
    }

    public void sendFile(String msg, File f) {
        CommandUtils.sendFile(msg, f, this.EVENT);
    }

    /**
     * Returns whether or not the author of this CommandEvent is a bot owner.
     *
     * @return Whether or not the author is elevated.
     */
    public boolean isElevated() {
        return IS_ELEVATED;
    }

    /**
     * Returns the MCHelper instance the CommandEvent came from.
     *
     * @return The MCHelper instance.
     */
    public MCHelper getMCHelper() {
        return MCHELPER;
    }

    /**
     * Returns the associated Database with this CommandEvent.
     *
     * @return The database.
     */
    public Database getDatabase() {
        return DATABASE;
    }

    /**
     * Returns the associated Settings with this CommandEvent.
     *
     * @return The settings.
     */
    public Settings getSettings() {
        return MCHELPER.getSettings();
    }

    protected Logger getLogger() {
        return LOGGER;
    }

    public String getCommandName() {
        return COMMAND_NAME;
    }

    public LocalDateTime getExecutedWhen() {
        return EXECUTED_WHEN;
    }
}
