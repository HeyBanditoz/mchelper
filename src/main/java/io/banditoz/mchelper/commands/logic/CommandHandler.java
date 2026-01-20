package io.banditoz.mchelper.commands.logic;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

import io.banditoz.mchelper.commands.HelpCommand;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.stats.service.StatsRecorder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton entry-point for execution of <i>text-based</i> commands.
 */
@Singleton
public class CommandHandler extends ListenerAdapter implements AutoCloseable {
    private final ThreadPoolExecutor threadPoolExecutor;
    private final ConfigurationProvider configurationProvider;
    private final StatsRecorder statsRecorder;
    private final InteractionListener interactionListener;
    /** The command map. String is the command name (what the user types) and Command is the command. */
    private final Map<String, Command> commands;

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private int commandsRun;
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    @Inject
    public CommandHandler(ThreadPoolExecutor threadPoolExecutor,
                          ConfigurationProvider configurationProvider,
                          StatsRecorder statsRecorder,
                          InteractionListener interactionListener,
                          List<Command> commands) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.configurationProvider = configurationProvider;
        this.statsRecorder = statsRecorder;
        this.interactionListener = interactionListener;
        this.commands = Stream.concat(Stream.of(new HelpCommand(commands)), commands.stream())
                .collect(Collectors.toMap(Command::commandName, identity()));
        log.info("{} commands registered.", commands.size());
    }

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
            if (c.canExecute(event.getAuthor())) {
                threadPoolExecutor.execute(() -> {
                    try {
                        Stat s = c.execute(event, kind, interactionListener, this, configurationProvider);
                        if (s.getStatus() == Status.SUCCESS) {
                            commandsRun++;
                        }
                        log.info(s.getLogMessage());
                        statsRecorder.record(s);
                    } catch (Exception ex) {
                        log.error("Unhandled exception in command execution. This should never happen. author=" + event.getAuthor() + " args='" + event.getMessage().getContentRaw() + "' command=" + c, ex);
                    }
                });
            }
            else {
                CommandUtils.sendReply(String.format("User <%s> does not have permission to run this command!",
                        event.getAuthor()), event);
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
            prefix = configurationProvider.getValue(Config.PREFIX, e.getGuild()).charAt(0);
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

    public int getCommandsRun() {
        return commandsRun;
    }

    @Override
    public void close() throws Exception {
        log.info("Shutting down CommandHandler...");
        isShutdown.set(true);
    }
}
