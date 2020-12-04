package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
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
 *          return "ping";
 *      }
 *
 *      public Help getHelp() {
 *          return new Help(commandName(), false).withParameters(null)
 *             .withDescription("Returns \"Pong!\"");
 *      }
 *
 *      protected Status onCommand(CommandEvent ce) throws Exception {
 *          ce.sendReply("Pong!");
 *          return Status.SUCCESS;
 *      }
 * }
 * </pre>
 *
 * @see CommandHandler
 */
public abstract class Command {
    protected abstract Status onCommand(CommandEvent ce) throws Exception;
    public abstract String commandName();
    public abstract Help getHelp();

    /**
     * Return the required Discord permissions to run this command. Note that a user must have <b>all</b> the
     * permissions in the EnumSet.
     *
     * @return The required permissions.
     */
    protected EnumSet<Permission> getRequiredPermissions() {
        return EnumSet.noneOf(Permission.class);
    }

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
    protected Stat execute(MessageReceivedEvent e, MCHelper MCHelper) {
        CommandEvent ce = new CommandEvent(e, LOGGER, MCHelper, this.getClass().getSimpleName());
        long before = System.nanoTime();
        // bot owners can bypass permission checks
        if (!CommandPermissions.isBotOwner(e.getAuthor(), MCHelper.getSettings())) {
            if (!e.getMember().getPermissions().containsAll(getRequiredPermissions())) {
                e.getMessage().addReaction("\uD83D\uDD34").queue();
                return new LoggableCommandEvent(ce, (int) ((System.nanoTime()) - before) / 1000000, Status.NO_PERMISSION);
            }
        }

        if (handleCooldown(e.getAuthor().getId())) {
            e.getChannel().sendTyping().queue();
            try {
                Status status = onCommand(ce);
                long after = System.nanoTime() - before;
                return new LoggableCommandEvent(ce, (int) (after / 1000000), status);
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(e, ex, LOGGER);
                return new LoggableCommandEvent(ce, (int) ((System.nanoTime() - before) / 1000000), Status.EXCEPTIONAL_FAILURE);
            } catch (Throwable t) {
                CommandUtils.sendThrowableMessage(e, t, LOGGER);
                if (t instanceof OutOfMemoryError) {
                    System.gc();
                }
                throw t; // rethrow
            }
        }
        else {
            e.getMessage().addReaction("⏲️").queue();
            return new LoggableCommandEvent(ce, (int) ((System.nanoTime() - before) / 1000000), Status.COOLDOWN);
        }
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
