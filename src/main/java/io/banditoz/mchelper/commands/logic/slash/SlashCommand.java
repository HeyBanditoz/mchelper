package io.banditoz.mchelper.commands.logic.slash;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CooldownType;
import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlashCommand {
    private final Command command;
    /**
     * {@link MethodHandle} to the onSlashCommand method contained within the {@link Command} class.
     * Essentially this lets us call the <code>onSlashCommand</code> method and get some JIT optimizations too.
     *
     */
    private final MethodHandle osc;
    private final Method oscMethod;
    private final SlashCommandData slashCommandData;

    private static final Logger log = LoggerFactory.getLogger(SlashCommand.class);
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public SlashCommand(Command command) throws MissingMethodException {
        this.command = command;
        Method onSlash = Arrays.stream(command.getClass().getDeclaredMethods())
                .filter(method -> "onSlashCommand".equals(method.getName()))
                .filter(method -> Arrays.stream(method.getAnnotations())
                        .map(Annotation::annotationType)
                        .anyMatch(aClass -> aClass == Slash.class))
                .filter(method -> method.getReturnType().equals(Status.class))
                .filter(method -> method.getParameters().length > 0)
                .filter(method -> method.getParameters()[0].getType().equals(SlashCommandEvent.class))
                .findFirst()
                .orElseThrow(MissingMethodException::new);
        this.oscMethod = onSlash;
        try {
            this.osc = lookup.unreflect(onSlash);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        this.slashCommandData = initDataForCommand();
    }

    protected Stat invoke(SlashCommandInteractionEvent e, ScheduledExecutorService ses) throws Throwable {
        // TODO this argument matching and filling needs unit tests so very badly
        LinkedList<OptionMapping> actualParams = new LinkedList<>(e.getOptions()); // mutable list we want to insert in the middle of
        List<OptionData> formalParams = slashCommandData.getOptions();

        // check for type confusion
        // is this even possible? do we trust Discord?
        for (OptionMapping actualParam : actualParams) {
            for (OptionData formalParam : formalParams) {
                if (!formalParam.getName().equals(actualParam.getName())) {
                    continue;
                }
                if (!actualParam.getType().equals(formalParam.getType())) {
                    throw new IllegalArgumentException("Type mismatch for argument %s. Expected %s but got %s instead."
                            .formatted(actualParam.getName(), actualParam.getType(), formalParam.getType()));
                }
            }
        }

        // missing arguments in the middle, fill them in
        // it would be nice if Discord sent the missing arguments, but oh well
        if (actualParams.size() < formalParams.size()) {
            // TODO guard against nulls where the method param is not annotated with @Nullable (can we trust Discord to do this?)
            for (int i = 0; i < formalParams.size(); i++) {
                OptionData formalParam = formalParams.get(i);
                if (actualParams.size() == i) {
                    // fill null case
                    actualParams.add(i, null);
                }
                else {
                    OptionMapping actualOptionMapping = actualParams.get(i);
                    if (!actualOptionMapping.getName().equals(formalParam.getName())) {
                        // argument would hopefully be shifted later, but we aren't checking for that exactly...
                        actualParams.add(i, null);
                    }
                }
            }
        }
        Object[] finalMappedCommandParameters = actualParams.stream()
                .map(SlashCommandUtils::getValueFromOption)
                .toArray();
        ISnowflake cooldownEntity = command.getCooldown() != null && command.getCooldown().getType() == CooldownType.PER_USER
                ? e.getUser() : e.isFromGuild()
                ? e.getGuild() : null;

        SlashCommandEvent scEvent = new SlashCommandEvent(e, this, ses, finalMappedCommandParameters);
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
            e.reply("You do not have permission to run this command!").setEphemeral(true).queue();
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime()) - before) / 1000000, Status.NO_PERMISSION, Kind.SLASH, beforeLdt);
        }

        try {
            Status status = invokeSlashCommandMethod(scEvent, finalMappedCommandParameters);
            if (status != Status.SUCCESS) {
                command.removeFromCooldown(cooldownEntity);
            }
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime() - before) / 1000000), status, Kind.SLASH, beforeLdt);
        } catch (Exception ex) {
            command.removeFromCooldown(cooldownEntity);
            log.error("Exception! <" + e.getUser() + "> " + e.getOptions(), ex);
            String msg = "**Status: Calamitous:** " + StringUtils.truncate(MarkdownSanitizer.escape(ex.toString()), 500, true);
            scEvent.sendReply(msg);
            return new LoggableSlashCommandEvent(scEvent, (int) ((System.nanoTime() - before) / 1000000), Status.EXCEPTIONAL_FAILURE, Kind.SLASH, beforeLdt);
        }
    }

    /**
     * Runs a {@link Command}'s <code>onSlashCommand</code> method.
     *
     * @param scEvent The invoking event.
     * @param p       Array of parameters. Can be empty, meaning the command doesn't take any arguments.
     * @return Status of the command.
     * @throws Throwable The command threw an exception while attempting to execute it. Propagates up to the
     *                   {@link SlashCommandHandler} for auditing the failure.
     */
    private Status invokeSlashCommandMethod(SlashCommandEvent scEvent, Object[] p) throws Throwable {
        // we need to switch on args length here, as java doesn't see an array as variadic args
        // when mapping to formal method args for reflective invocation (or maybe it never does?)
        // either way, demons are here, sorry!
        return (Status) switch (p.length) {
            case 0 -> osc.invoke(command, scEvent);
            case 1 -> osc.invoke(command, scEvent, p[0]);
            case 2 -> osc.invoke(command, scEvent, p[0], p[1]);
            case 3 -> osc.invoke(command, scEvent, p[0], p[1], p[2]);
            case 4 -> osc.invoke(command, scEvent, p[0], p[1], p[2], p[3]);
            case 5 -> osc.invoke(command, scEvent, p[0], p[1], p[2], p[3], p[4]);
            case 6 -> osc.invoke(command, scEvent, p[0], p[1], p[2], p[3], p[4], p[5]);
            default -> throw new IllegalArgumentException("Too many args!");
        };
    }

    /**
     * Generates a Discord {@link SlashCommandData slash command definition} for this {@link SlashCommand} wrapping the
     * {@link Command}
     *
     * @return Discord data.
     */
    private SlashCommandData initDataForCommand() {
        SlashCommandData commandData = Commands.slash(command.commandName(), StringUtils.truncate(command.getHelp().getDescription(), 97, false));

        // parameter loop
        for (int i = 1; i < oscMethod.getParameters().length; i++) {
            Parameter parameter = oscMethod.getParameters()[i];
            OptionType optionType = SlashCommandUtils.getOptionTypeFromClass(parameter.getType());
            String name = parameter.getName();
            String desc = parameter.getName();
            boolean isRequired = true;

            // annotations on method parameter
            for (Annotation annotation : parameter.getAnnotations()) {
                switch (annotation) {
                    case Param p -> {
                        if (!p.name().isEmpty()) {
                            name = p.name();
                        }
                        if (!p.desc().isEmpty()) {
                            desc = p.desc();
                        }
                    }
                    case Nullable n -> {
                        if (parameter.getType().isPrimitive()) {
                            throw new IllegalArgumentException("Java does not support nullable primitives, please use" +
                                    " the boxed type instead (i.e. int -> Integer) or don't annotate your parameter with" +
                                    " @Nullable. For parameter name " + parameter.getName());
                        }
                        isRequired = false;
                    }
                    default -> {}
                }
            }
            commandData.addOption(optionType, name.toLowerCase(), desc, isRequired);
            if (!command.getRequiredPermissions().isEmpty()) {
                commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getRequiredPermissions()));
            }
        }
        return commandData;
    }

    /** @return The definition for this slash command, for Discord. */
    public SlashCommandData getDataForCommand() {
        return slashCommandData;
    }

    /** @return The actual {@link Command} this SlashCommand has.*/
    public Command getCommand() {
        return command;
    }
}
