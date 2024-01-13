package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.HelpCommand;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.ClassUtils;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandHandler extends ListenerAdapter {
    /** The command map. String is the command name (what the user types) and Command is the command. */
    private final Map<String, Command> commands = new HashMap<>();
    private final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private final MCHelper MCHELPER;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private int commandsRun;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        go(event, Kind.TEXT);
    }

    /**
     * Attempt to run a command that is currently unknown at invocation time.
     * @param event The {@link MessageReceivedEvent} to use for command execution.
     * @param kind  The source of the command.
     */
    protected void go(@Nonnull MessageReceivedEvent event, Kind kind) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong() || event.getMessage().isWebhookMessage())
            return; // don't execute own commands, or webhook messages
        getCommandByEvent(event).ifPresent(c -> {
            if (isShutdown.get()) {
                event.getChannel().sendMessage("WARN: This CommandHandler is being shutdown. No new commands will be accepted. The bot is most likely rebooting. Try again later.").queue();
                return;
            }
            if (c.canExecute(event, MCHELPER)) {
                MCHELPER.getThreadPoolExecutor().execute(() -> {
                    try {
                        Stat s = c.execute(event, MCHELPER, kind);
                        if (s.getStatus() == Status.SUCCESS) {
                            commandsRun++;
                        }
                        LOGGER.info(s.getLogMessage());
                        MCHELPER.getStatsRecorder().record(s);
                    } catch (Exception ex) {
                        LOGGER.error("Unhandled exception in command execution. This should never happen. author=" + event.getAuthor() + " args='" + event.getMessage().getContentRaw() + "' command=" + c, ex);
                    }
                });
            }
        });
    }

    protected Optional<Command> getCommandByEvent(MessageReceivedEvent e) {
        String[] args = CommandUtils.commandArgs(e.getMessage().getContentDisplay());
        if (args.length == 0) {
            return Optional.empty();
        }
        char prefix = '!';
        if (e.isFromGuild()) {
            prefix = MCHELPER.getConfigurationProvider().getValue(Config.PREFIX, e.getGuild()).charAt(0);
        }
        if (args[0].charAt(0) != prefix) {
            return Optional.empty();
        }
        return Optional.ofNullable(commands.get(args[0].substring(1).toLowerCase())); // remove the prefix from the command arg
    }

    public Collection<Command> getCommands() {
        return commands.values();
    }

    public boolean removeCommandByName(String name) {
        return commands.remove(name) != null;
    }

    public CommandHandler(MCHelper MCHelper) throws Exception {
        this.MCHELPER = MCHelper;
        LOGGER.info("Registering commands...");
        long before = System.currentTimeMillis();
        Set<Class<? extends Command>> classes = ClassUtils.getAllSubtypesOf(Command.class);
        for (Class<? extends Command> clazz : classes) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                // We have to catch ElevatedCommand here; that should be the only class, though.
                continue;
            }
            if (clazz.equals(HelpCommand.class)) {
                // We manually add this at the end, as we pass in a list of Commands to HelpCommand at the end.
                continue;
            }
            Command c = clazz.getDeclaredConstructor().newInstance();
            Requires r = c.getClass().getAnnotation(Requires.class);
            if (r == null) {
                commands.put(c.commandName(), c);
                continue;
            }
            if (r.database()) {
                if (Database.isConfigured()) {
                    commands.put(c.commandName(), c);
                }
                else {
                    LOGGER.warn("Not registering " + clazz.getSimpleName() + " as the database is not configured.");
                }
            }
            else if (!r.config().isEmpty()) {
                String s = io.avaje.config.Config.getNullable(r.config());
                if (s != null) {
                    commands.put(c.commandName(), c);
                }
                else {
                    LOGGER.warn("Not registering {} as {} is null.", clazz.getSimpleName(), r.config());
                }
            }
        }
        HelpCommand help = new HelpCommand(commands.values());
        commands.put(help.commandName(), help);
        LOGGER.info(commands.size() + " commands registered in " + (System.currentTimeMillis() - before) + " ms.");
    }

    public int getCommandsRun() {
        return commandsRun;
    }

    public void dontAcceptNewCommands() {
        isShutdown.set(true);
    }
}
