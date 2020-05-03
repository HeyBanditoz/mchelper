package io.banditoz.mchelper.commands.logic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
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

    /**
     * Return this command's cooldown in seconds. Override and return what you want the cooldown to be.
     * @return The cooldown.
     */
    protected int getCooldown() {
        return 0;
    }
    protected HashMap<String, Instant> cooldowns = getCooldown() > 0 ? new HashMap<>() : null;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final static ThreadPoolExecutor ES;

    static {
        Settings s = SettingsManager.getInstance().getSettings();
        // we do this instead of Executors.newFixedThreadPool so we can get the current number of waiting threads.
        ES = new ThreadPoolExecutor(s.getCommandThreads(), s.getCommandThreads(),
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("Command-%d").build());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (containsCommand(e)) {
            go(e);
        }
    }

    /**
     * Runs onCommand() in parallel.
     */
    protected void go(MessageReceivedEvent e) {
        if (!e.isFromGuild()) return; // TODO I'm incredibly lazy and should actually fix this sometime.
        if (e.getJDA().getSelfUser().getId().equals(e.getAuthor().getId())) return; // don't execute own commands.
        if (handleCooldown(e.getAuthor().getId())) {
            e.getChannel().sendTyping().queue();
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
        else {
            e.getMessage().addReaction("⏲️").queue();
        }
    }

    protected boolean containsCommand(MessageReceivedEvent e) {
        String[] args = CommandUtils.commandArgs(e.getMessage().getContentDisplay());
        if (args.length == 0) {
            return false;
        }
        char prefix = new GuildConfigDaoImpl().getConfig(e.getGuild()).getPrefix();
        String expected = prefix + commandName();
        return expected.equalsIgnoreCase(args[0]);
    }

    /**
     * Checks if the user is on cooldown.
     * @param id The ID to check.
     * @return true if they are allowed to run the command, false if they are still on cooldown.
     */
    private boolean handleCooldown(String id) {
        if (getCooldown() > 0) {
            Instant cooldown = cooldowns.get(id);
            if (cooldown == null) {
                Instant instant = Instant.now().plus(getCooldown(), ChronoUnit.SECONDS);
                cooldowns.put(id, instant);
                return true;
            } else if (Instant.now().isAfter(cooldown)) {
                cooldowns.replace(id, Instant.now().plus(getCooldown(), ChronoUnit.SECONDS));
                return true;
            } else {
                return false;
            }
        }
        else {
            return true;
        }
    }
}
