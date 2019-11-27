package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

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
 *          public Help getHelp() {
 *              return new Help(commandName(), false).withParameters(null)
 *                 .withDescription("Returns \"Pong!\"");
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
    public abstract Help getHelp();

    protected String commandArgsString;
    protected String[] commandArgs;
    protected MessageReceivedEvent e;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    // we do this instead of Executors.newFixedThreadPool so we can get the current number of waiting threads.
    protected final static ThreadPoolExecutor ES = new ThreadPoolExecutor(4, 4,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (containsCommand(e)) {
            initialize(e);
            go();
        }
    }

    /**
     * Initialize variables.
     */
    protected void initialize(MessageReceivedEvent e) {
        this.e = e;
        this.commandArgs = CommandUtils.commandArgs(e.getMessage());
        this.commandArgsString = CommandUtils.generateCommandArgsString(e);
    }

    /**
     * Runs onCommand() in parallel.
     */
    protected void go() {
        if (e.getJDA().getSelfUser().getId().equals(e.getAuthor().getId())) return; // don't execute own commands.
        this.e.getChannel().sendTyping().queue();
        ES.execute(() -> {
            try {
                long before = System.nanoTime();
                onCommand();
                long after = System.nanoTime() - before;
                LOGGER.debug("Command ran in " + (after / 1000000) + " ms.");
            } catch (Exception ex) {
                sendExceptionMessage(ex, false);
            }
        });
        LOGGER.debug(ES.toString());
    }

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex, boolean caught) {
        CommandUtils.sendExceptionMessage(this.e, ex, LOGGER, caught, false);
    }

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex) {
        CommandUtils.sendExceptionMessage(this.e, ex, LOGGER, true, false);
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
