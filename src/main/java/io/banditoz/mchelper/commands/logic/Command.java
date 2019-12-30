package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Database;
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
 * To send replies, you must leverage the CommandEvent class that should be passed with the onCommand() method.
 *
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
 *          protected void onCommand(CommandEvent ce) {
 *              ce.sendReply("Pong!");
 *          }
 *     }
 * </pre>
 *
 * @see io.banditoz.mchelper.MCHelper
 */
public abstract class Command extends ListenerAdapter {
    protected abstract void onCommand(CommandEvent ce);
    public abstract String commandName();
    public abstract Help getHelp();
    protected MessageReceivedEvent e;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    // we do this instead of Executors.newFixedThreadPool so we can get the current number of waiting threads.
    protected final static ThreadPoolExecutor ES = new ThreadPoolExecutor(4, 4,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (containsCommand(e)) {
            this.e = e;
            go();
        }
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
                onCommand(new CommandEvent(e, LOGGER));
                long after = System.nanoTime() - before;
                LOGGER.debug("Command ran in " + (after / 1000000) + " ms.");
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(e, ex, LOGGER, false, false);
            }
        });
        LOGGER.debug(ES.toString());
    }

    protected boolean containsCommand(MessageReceivedEvent e) {
        String[] args = CommandUtils.commandArgs(e.getMessage().getContentDisplay());
        char prefix = Database.getInstance().getGuildDataById(e.getGuild()).getPrefix();
        String expected = prefix + commandName();
        return expected.equals(args[0]);
    }
}
