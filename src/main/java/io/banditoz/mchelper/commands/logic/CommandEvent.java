package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Class which holds the MessageReceivedEvent and command arguments.
 */
public class CommandEvent {
    private final String commandArgsString;
    private final String[] commandArgs;
    private final MessageReceivedEvent e;
    private final Logger logger;
    private final Guild guild;

    public CommandEvent(@NotNull MessageReceivedEvent e, Logger logger) {
        this.e = e;
        this.commandArgsString = CommandUtils.generateCommandArgsString(e);
        this.commandArgs = CommandUtils.commandArgs(e.getMessage());
        this.logger = logger;
        this.guild = e.getGuild();
    }

    /**
     * Grabs the command arguments formatted as a String.
     * @return The arguments.
     */
    public String getCommandArgsString() {
        return commandArgsString;
    }

    /**
     * Grabs the command arguments as an array.
     * @return The arguments.
     */
    public String[] getCommandArgs() {
        return commandArgs;
    }

    /**
     * Grabs the MessageReceivedEvent.
     * @return the MessageReceivedEvent.
     */
    public MessageReceivedEvent getEvent() {
        return e;
    }

    public Guild getGuild() {
        return guild;
    }

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex, boolean caught) {
        CommandUtils.sendExceptionMessage(this.e, ex, logger, caught, false);
    }

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex) {
        CommandUtils.sendExceptionMessage(this.e, ex, logger, true, false);
    }

    /**
     * Sends a reply.
     * @param msg The reply.
     */
    public void sendReply(String msg) {
        CommandUtils.sendReply(msg, e);
    }

    /**
     * Sends an EmbedReply.
     * @param me The reply.
     */
    public void sendEmbedReply(MessageEmbed me) {
        e.getChannel().sendMessage(me).queue();
    }
}
