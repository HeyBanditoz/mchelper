package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.HelpCommand;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.ClassUtils;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class CommandHandler extends ListenerAdapter {
    /** The command map. String is the command name (what the user types) and Command is the command. */
    private final Map<String, Command> commands = new HashMap<>();
    private final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private final MCHelper MCHELPER;
    private int commandsRun;

    private final Random random = new Random();
    private final BidiMap<Long, Captcha> captchas = new DualHashBidiMap<>();

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong())
            return; // don't execute own commands
        getCommandByEvent(event).ifPresent(c -> {
            if (captchas.get(event.getAuthor().getIdLong()) == null) {
                if (random.nextBoolean()) {
                    // uh oh! due to extremely intelligent bot detection algorithms we have proof beyond a reasonable doubt
                    // that the user attempting to execute a command is a BOT! we must check and make sure!
                    LOGGER.info("User {} caught by captcha check.", event.getAuthor());
                    Captcha captcha = new Captcha(event.getAuthor(), c, event);
                    captchas.put(event.getAuthor().getIdLong(), captcha);
                }
                else {
                    // they're free to go this time!
                    execute(event, c);
                }
            }
        });
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        BidiMap<Captcha, Long> local = captchas.inverseBidiMap();
        for (Captcha captcha : local.keySet()) {
            if (captcha.getCorrectUUID().toString().equals(event.getButton().getId())) {
                if (captcha.getChallenger() == event.getUser()) { // the original capt?pcha invoker is the only one that can attempt to pass it
                    local.remove(captcha);
                    String oldText = event.getMessage().getContentRaw();
                    event.getMessage()
                            .editMessage(oldText + '\n' + "*Captcha passed.* Great job!")
                            .setActionRows(Collections.emptyList())
                            .queue();
                    LOGGER.info("User {} PASSED the captcha check.", captcha.getEvent().getAuthor());
                    execute(captcha.getEvent(), captcha.getCommand());
                }
            }
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        BidiMap<Captcha, Long> local = captchas.inverseBidiMap();
        for (Captcha captcha : local.keySet()) {
            if (captcha.getMessage().getIdLong() == event.getMessageIdLong()) {
                captcha.sendMessage(true);
                LOGGER.warn("User {} IS A BOT SUPPORTER!!!!11!1!11!!11!", captcha.getEvent().getAuthor());
                return;
            }
        }
    }

    private void execute(MessageReceivedEvent event, Command c) {
        if (c.canExecute(event, MCHELPER)) {
            MCHELPER.getThreadPoolExecutor().execute(() -> {
                Stat s = c.execute(event, MCHELPER);
                if (s.getStatus() == Status.SUCCESS) {
                    commandsRun++;
                }
                LOGGER.info(s.getLogMessage());
                MCHELPER.getStatsRecorder().record(s);
            });
        }
    }

    protected Optional<Command> getCommandByEvent(MessageReceivedEvent e) {
        String[] args = CommandUtils.commandArgs(e.getMessage().getContentDisplay());
        if (args.length == 0) {
            return Optional.empty();
        }
        char prefix = '!';
        if (e.isFromGuild()) {
            prefix = new GuildConfigDaoImpl(MCHELPER.getDatabase()).getConfig(e.getGuild()).getPrefix();
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
        Settings settings = MCHelper.getSettings();
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
            else if (!r.settingsMethod().isEmpty()) {
                // This is hacky, but essentially the "Requires" annotation holds a field called method(), which
                // returns the underlying method name in Settings, so we dynamically invoke it here to see if it's
                // not null, which means the user configured that setting. We have to do it this way as you can't
                // have a method reference in an annotation. :(
                Method m = settings.getClass().getDeclaredMethod(r.settingsMethod());
                String s = (String) m.invoke(settings);
                if (s != null) {
                    commands.put(c.commandName(), c);
                }
                else {
                    LOGGER.warn("Not registering " + clazz.getSimpleName() + " as " + r.settingsMethod() + " is null or default.");
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
}
