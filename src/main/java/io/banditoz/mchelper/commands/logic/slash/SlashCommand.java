package io.banditoz.mchelper.commands.logic.slash;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nullable;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CooldownType;
import io.banditoz.mchelper.commands.logic.params.*;
import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a {@link BoundCommand} so it can be registered with, and invoked by, Discord as a slash command.
 *
 * @see BoundCommand
 */
public class SlashCommand {
    private final BoundCommand command;
    private final SlashCommandData slashCommandData;

    private static final Logger log = LoggerFactory.getLogger(SlashCommand.class);

    /**
     * @param command The command to expose as a slash command.
     * @throws NotBindableException If the command is not a {@link BoundCommand}, and so has no parameter
     *                              declaration to build a slash command out of.
     */
    public SlashCommand(Command command) throws NotBindableException {
        if (!(command instanceof BoundCommand bound)) {
            throw new NotBindableException();
        }
        this.command = bound;
        this.slashCommandData = initDataForCommand(bound);
    }

    protected Stat invoke(SlashCommandInteractionEvent e, ScheduledExecutorService ses) throws Throwable {
        ISnowflake cooldownEntity = command.getCooldown() != null && command.getCooldown().getType() == CooldownType.PER_USER
                ? e.getUser() : e.isFromGuild()
                ? e.getGuild() : null;

        // Constructed before anything that can fail or block: this is what schedules the deferred reply
        // against Discord's three second window.
        SlashCommandEvent scEvent = new SlashCommandEvent(e, this, ses,
                e.getOptions().stream().map(OptionMapping::getAsString).toArray());
        long before = System.nanoTime();
        LocalDateTime beforeLdt = LocalDateTime.now();

        EnumSet<Permission> memberPermissions = e.getMember() == null ? EnumSet.noneOf(Permission.class) : e.getMember().getPermissions();
        if (!command.guildPermissionToExecute(e.getUser(), memberPermissions)) {
            e.reply("You do not have permission to run this command!").setEphemeral(true).queue();
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime()) - before) / 1000000, Status.NO_PERMISSION, Kind.SLASH, beforeLdt);
        }

        if (!command.placeOnCooldown(new SlashEventAdapter(e))) {
            e.reply("You are on cooldown.").setEphemeral(true).queue(); // TODO say when cooldown expires
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime()) - before) / 1000000, Status.COOLDOWN, Kind.SLASH, beforeLdt);
        }

        if (!command.canExecute(e.getUser())) {
            e.reply("You do not have permission to run this command!").setEphemeral(true).queue(null, null);
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime()) - before) / 1000000, Status.NO_PERMISSION, Kind.SLASH, beforeLdt);
        }

        try {
            Object[] values = SlashBinder.bind(command.paramSpec(), e,
                    new BindContext(e.getJDA(), e.getGuild(), e.getUser()));
            Status status = command.handler().invoker().invoke(scEvent, values);
            if (status != Status.SUCCESS) {
                command.removeFromCooldown(cooldownEntity);
            }
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime() - before) / 1000000), status, Kind.SLASH, beforeLdt);
        } catch (BindException ex) {
            command.removeFromCooldown(cooldownEntity);
            scEvent.sendReply(ex.render(command.commandName(), command.paramSpec()));
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime() - before) / 1000000), Status.BINDING_FAIL, Kind.SLASH, beforeLdt);
        } catch (Exception ex) {
            command.removeFromCooldown(cooldownEntity);
            log.error("Exception! <" + e.getUser() + "> " + e.getOptions(), ex);
            String msg = "**Status: Calamitous:** " + StringUtils.truncate(MarkdownSanitizer.escape(ex.toString()), 500, true);
            scEvent.sendReply(msg);
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime() - before) / 1000000), Status.EXCEPTIONAL_FAILURE, Kind.SLASH, beforeLdt);
        }
    }

    /**
     * Generates a Discord {@link SlashCommandData slash command definition} from the command's parameter
     * declaration.
     *
     * @param command The command to render.
     * @return Discord data.
     */
    private static SlashCommandData initDataForCommand(BoundCommand command) {
        SlashCommandData commandData = Commands.slash(command.commandName(),
                StringUtils.truncate(command.getHelp().getDescription(), 97, false));

        for (Arg<?> param : command.paramSpec().params()) {
            OptionData option = new OptionData(
                    param.type().optionType(),
                    param.name(),
                    StringUtils.truncate(param.desc(), 97, false),
                    param.required());
            if (param.type() instanceof ParamTypes.EnumType<?> enumType) {
                for (Enum<?> constant : enumType.javaType().getEnumConstants()) {
                    option.addChoice(constant.name().toLowerCase(), constant.name());
                }
            }
            if (param.completer() != null) {
                // JDA rejects this itself, but without saying which command or parameter was at fault
                if (!param.type().optionType().canSupportChoices() || !option.getChoices().isEmpty()) {
                    throw new IllegalArgumentException(command.commandName() + " autocompletes parameter "
                            + param.name() + ", but Discord only autocompletes free text and number options.");
                }
                option.setAutoComplete(true);
            }
            commandData.addOptions(option);
        }
        if (!command.getRequiredPermissions().isEmpty()) {
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getRequiredPermissions()));
        }
        return commandData;
    }

    /** @return The definition for this slash command, for Discord. */
    public SlashCommandData getDataForCommand() {
        return slashCommandData;
    }

    /**
     * @param optionName The option the user is typing, from {@link
     *                   net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent#getFocusedOption()}.
     * @return What suggests values for that option, or null if it isn't autocompleted.
     */
    @Nullable
    public Completer completerFor(String optionName) {
        for (Arg<?> param : command.paramSpec().params()) {
            if (param.name().equals(optionName)) {
                return param.completer();
            }
        }
        return null;
    }

    /** @return The actual {@link Command} this SlashCommand has.*/
    public Command getCommand() {
        return command;
    }
}
