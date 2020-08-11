package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.MCHelperImpl;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

/**
 * Represents the abstract class for any Command the bot may have. Note any command you implement
 * you must register in MCHelper.java. All commands will be multithreaded. All exceptions will be caught,
 * and a message sent to Discord showing the classpath and description of the exception.
 * <p>
 * To send replies, you must leverage the CommandEvent class that should be passed with the onCommand() method.
 * <p>
 * <p>
 * An example command could be
 * <pre>
 * public class PingCommand extends Command {
 *      public String CommandName() {
 *          return "!ping";
 *      }
 *
 *      public Help getHelp() {
 *          return new Help(commandName(), false).withParameters(null)
 *             .withDescription("Returns \"Pong!\"");
 *      }
 *
 *      protected void onCommand(CommandEvent ce) throws Exception {
 *          ce.sendReply("Pong!");
 *      }
 * }
 * </pre>
 *
 * @see CommandHandler
 */
public abstract class Command {
    protected abstract void onCommand(CommandEvent ce) throws Exception;
    public abstract String commandName();
    public abstract Help getHelp();

    /**
     * Return this command's cooldown in seconds. Override and return what you want the cooldown to be.
     *
     * @return The cooldown.
     */
    protected int getCooldown() {
        return 0;
    }

    protected HashMap<String, Instant> cooldowns = getCooldown() > 0 ? new HashMap<>() : null;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Method that can be overridden in subclasses to check conditions before execution
     *
     * @param e The MessageReceivedEvent to execute
     * @see ElevatedCommand
     */
    protected boolean canExecute(MessageReceivedEvent e, MCHelper mcHelper) {
        return true;
    }

    /**
     * Runs onCommand() in parallel.
     */
    protected void execute(MessageReceivedEvent e, MCHelper MCHelper) {
        if (handleCooldown(e.getAuthor().getId())) {
            e.getChannel().sendTyping().queue();
            try {
                LOGGER.info(String.format("Executing command: <%s@%s> %s",
                        e.getAuthor().toString(), e.getChannel().toString(), e.getMessage().getContentDisplay()));
                long before = System.nanoTime();
                onCommand(new CommandEvent(e, LOGGER, MCHelper));
                long after = System.nanoTime() - before;
                LOGGER.debug("Command ran in " + (after / 1000000) + " ms.");
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(e, ex, LOGGER);
            }
        }
        else {
            e.getMessage().addReaction("⏲️").queue();
        }
    }

    protected boolean containsCommand(MessageReceivedEvent e, Database database) {
        String[] args = CommandUtils.commandArgs(e.getMessage().getContentDisplay());
        if (args.length == 0) {
            return false;
        }
        char prefix = '!';
        if (e.isFromGuild()) {
            prefix = new GuildConfigDaoImpl(database).getConfig(e.getGuild()).getPrefix();
        }
        String expected = prefix + commandName();
        return expected.equalsIgnoreCase(args[0]);
    }

    /**
     * Checks if the user is on cooldown.
     *
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
            }
            else if (Instant.now().isAfter(cooldown)) {
                cooldowns.replace(id, Instant.now().plus(getCooldown(), ChronoUnit.SECONDS));
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }
}
