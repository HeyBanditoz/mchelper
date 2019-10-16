package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Command extends ListenerAdapter {
    protected abstract void onCommand();
    public abstract String commandName();
    protected String commandArgsString;
    protected String[] commandArgs;
    protected MessageReceivedEvent e;
    protected Logger logger;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (containsCommand(e)) {
            if (MCHelper.jda.getSelfUser().getId().equals(e.getAuthor().getId())) return; // don't execute own commands.
            initialize(e);
            try {
                this.e.getChannel().sendTyping().queue();
                long before = System.nanoTime();
                onCommand();
                long after = System.nanoTime() - before;
                logger.debug("Command with class " + getClass().getCanonicalName() + " ran in " + (after / 1000000) + " ms.");
            } catch (Exception ex) {
                sendExceptionMessage(ex, false);
            }
        }
    }

    /**
     * Initialize variables.
     */
    protected void initialize(MessageReceivedEvent e) {
        this.e = e;
        this.commandArgs = CommandUtils.commandArgs(e.getMessage());
        this.commandArgsString = CommandUtils.generateCommandArgsString(e);
        logger = LoggerFactory.getLogger(getClass());
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

    protected boolean containsCommand(MessageReceivedEvent e) {
        return CommandUtils.commandArgs(e.getMessage()).length > 0 && commandName().equalsIgnoreCase(CommandUtils.commandArgs(e.getMessage())[0]);
    }
}
