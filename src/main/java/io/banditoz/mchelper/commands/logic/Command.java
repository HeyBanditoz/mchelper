package io.banditoz.mchelper.commands.logic;

import java.util.EnumSet;

import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the abstract class for any Command the bot may have. All commands will be multithreaded, such that the
 * <i>execution</i> of the command is decoupled from the rest of JDA's event processing. All exceptions will be caught,
 * and a message sent to Discord showing the classpath and description of the exception.
 * <p>
 * To send replies, you must leverage the CommandEvent class that's passed with the onCommand() method.
 * <p>
 * <p>
 * An example command could be
 * <pre>
 * &#064;Singleton
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
 *
 *      &#064;Slash
 *      public Status onSlashCommand(SlashCommandEvent sce) throws Exception {
 *          sce.sendReply("Pong!");
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
    protected final Cooldown cooldown = getDefaultCooldown();

    protected Cooldown getDefaultCooldown() {
        return null; // eh, should probably use a constructor for each command that wants a cooldown, but whatever
    }

    public Cooldown getCooldown() {
        return cooldown;
    }

    /**
     * Return the required Discord permissions to run this command. Note that a user must have <b>all</b> the
     * permissions in the EnumSet.
     *
     * @return The required permissions.
     */
    public EnumSet<Permission> getRequiredPermissions() {
        return EnumSet.noneOf(Permission.class);
    }

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Method that can be overridden in subclasses to check conditions before execution
     *
     * @param e The user to check.
     * @see ElevatedCommand
     */
    public boolean canExecute(User e) {
        return true;
    }

    /**
     * @param e The user event to place on cooldown.
     * @return true if the user, for all means and purposes, executed the command, and may have been
     * placed on cooldown. False, if they were already on cooldown.
     */
    public boolean placeOnCooldown(UserEventAdapter e) {
        if (cooldown != null) {
            switch (cooldown.getType()) {
                case PER_USER -> {
                    if (!cooldown.handle(e.getUser())) {
                        return false;
                    }
                }
                case PER_GUILD -> {
                    if (!cooldown.handle(e.getMember())) {
                        return false;
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + cooldown.getType());
            }
        }
        return true;
    }

    public boolean guildPermissionToExecute(User user, EnumSet<Permission> requiredPermissions) {
        if (!CommandPermissions.isBotOwner(user)) {
            return requiredPermissions.containsAll(getRequiredPermissions());
        }
        return true;
    }

    /** Runs onCommand(). Called from {@link CommandHandler#onMessageReceived(MessageReceivedEvent)} asynchronously, or by replay logic. */
    protected Stat execute(MessageReceivedEvent e, Kind kind, InteractionListener interactionListener, CommandHandler commandHandler, ConfigurationProvider configurationProvider) {
        CommandEvent ce = new CommandEvent(e, LOGGER, this.getClass().getSimpleName(), interactionListener, commandHandler, configurationProvider);
        long before = System.nanoTime();

        EnumSet<Permission> memberPermissions = e.getMember() == null ? EnumSet.noneOf(Permission.class) : e.getMember().getPermissions();
        if (!guildPermissionToExecute(e.getAuthor(), memberPermissions)) {
            e.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDD34")).queue();
            return new LoggableCommandEvent(ce, (int) ((System.nanoTime()) - before) / 1000000, Status.NO_PERMISSION, kind);
        }

        if (!placeOnCooldown(new TextEventAdapter(e))) {
            e.getMessage().addReaction(Emoji.fromUnicode("⏲️")).queue();
            return new LoggableCommandEvent(ce, (int) ((System.nanoTime() - before) / 1000000), Status.COOLDOWN, kind);
        }

        e.getChannel().sendTyping().queue(unused -> {}, throwable -> {}); // silence sendTyping errors when Discord shuts that endpoint off
        ISnowflake entity = getCooldown() != null && getCooldown().getType() == CooldownType.PER_USER
                ? e.getAuthor() : e.isFromGuild()
                    ? e.getGuild() : null;
        try {
            Status status = onCommand(ce);
            if (status != Status.SUCCESS) {
                removeFromCooldown(entity);
            }
            long after = System.nanoTime() - before;
            return new LoggableCommandEvent(ce, (int) (after / 1000000), status, kind);
        } catch (Exception ex) {
            removeFromCooldown(entity);
            CommandUtils.sendExceptionMessage(e, ex, LOGGER);
            return new LoggableCommandEvent(ce, (int) ((System.nanoTime() - before) / 1000000), Status.EXCEPTIONAL_FAILURE, kind);
        } catch (Throwable t) {
            removeFromCooldown(entity);
            CommandUtils.sendThrowableMessage(e, t, LOGGER);
            if (t instanceof OutOfMemoryError) {
                System.gc();
            }
            throw t; // rethrow
        }
    }

    /**
     * Removes an entity from the cooldown list, if the cooldown object exists.
     *
     * @param entity The entity to remove.
     */
    public void removeFromCooldown(ISnowflake entity) {
        if (entity != null && cooldown != null) {
            cooldown.remove(entity);
        }
    }
}
