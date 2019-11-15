package io.banditoz.mchelper.commands;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the abstract class for any Command the bot may have. Note any command you implement
 * you must register in MCHelper.java. All commands will be multithreaded. All exceptions will be caught,
 * and a message sent to Discord showing the classpath and description of the exception.
 *
 * An example command could be
 * <pre>
 *     public class PingCommand extends Command {
 *          public String CommandName() {
 *              return "!ping";
 *          }
 *
 *          protected void onCommand() {
 *              sendReply("Pong!");
 *          }
 *     }
 * </pre>
 *
 * @see io.banditoz.mchelper.MCHelper
 */
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
            if (e.getJDA().getSelfUser().getId().equals(e.getAuthor().getId())) return; // don't execute own commands.
            initialize(e);
                this.e.getChannel().sendTyping().queue();
                Thread thread = new Thread(() -> {
                    try {
                        long before = System.nanoTime();
                        onCommand();
                        long after = System.nanoTime() - before;
                        logger.debug("Command ran in " + (after / 1000000) + " ms.");
                    } catch (Exception ex) {
                        sendExceptionMessage(ex, false);
                    }
                });
                thread.start();
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
