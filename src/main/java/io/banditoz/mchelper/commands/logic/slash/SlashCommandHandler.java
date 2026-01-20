package io.banditoz.mchelper.commands.logic.slash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import io.avaje.config.Config;
import io.avaje.inject.PostConstruct;
import io.banditoz.mchelper.jda.OwnerMessenger;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.service.StatsRecorder;
import io.banditoz.mchelper.utils.StringUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton entry-point for execution of <i>slash-based</i> commands.
 */
@Singleton
public class SlashCommandHandler extends ListenerAdapter {
    private final ScheduledExecutorService ses;
    private final ThreadPoolExecutor tpe;
    private final StatsRecorder statsRecorder;
    private final JDA jda;
    private final OwnerMessenger ownerMessenger;
    private final Map<String, SlashCommand> commands = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(SlashCommandHandler.class);

    @Inject
    public SlashCommandHandler(ScheduledExecutorService ses,
                               ThreadPoolExecutor tpe,
                               StatsRecorder statsRecorder,
                               JDA jda,
                               OwnerMessenger ownerMessenger,
                               List<SlashCommand> commands) {
        this.ses = ses;
        this.tpe = tpe;
        this.statsRecorder = statsRecorder;
        this.ownerMessenger = ownerMessenger;
        this.jda = jda;
        for (SlashCommand slashCommand : commands) {
            this.commands.put(slashCommand.getCommand().commandName(), slashCommand);
        }
        log.info("{} slash commands registered.", this.commands.size());
    }

    // async
    @PostConstruct
    public void pushCommandsToDiscord() {
        List<SlashCommandData> list = this.commands.values().stream()
                .map(SlashCommand::getDataForCommand)
                .toList();
        List<Long> devGuilds = Config.list().ofLong("mchelper.slash-commands.dev-guilds");
        if (!devGuilds.isEmpty()) {
            log.info("Slash command dev guilds configured. Only configuring slash commands to appear in these guilds: {}", devGuilds);
            devGuilds.stream()
                    .map(jda::getGuildById)
                    .filter(Objects::nonNull)
                    .forEach(guild -> guild.updateCommands().addCommands(list)
                        .queue(cmds -> log.info("{} slash commands registered with {}", cmds.size(), guild), ex -> log.error("Exception registering slash commands with {}!", guild, ex))
                    );
        }
        else {
            // TODO delta diff check
            log.info("Globally registering slash commands...");
            jda.updateCommands().addCommands(list)
                    .queue(cmds -> log.info("{} slash commands globally registered.", cmds.size()), ex -> log.error("Exception registering global slash commands!", ex));
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        SlashCommand command = commands.get(event.getFullCommandName());
        if (command == null) {
            event.reply("Command not found within the bot. You probably shouldn't see this.").setEphemeral(true).queue();
            log.warn("{} tried to execute command \"{}\" not found within the bot.", event.getUser(), event.getFullCommandName());
            return;
        }
        tpe.execute(() -> {
            try {
                Stat s = command.invoke(event, ses);
                log.info(s.getLogMessage());
                statsRecorder.record(s);
            } catch (Throwable ex) {
                String msg = "**Exception caught before invocation of slash command occurred!** " + StringUtils.truncate(MarkdownSanitizer.escape(ex.toString()), 500, true);
                event.reply(msg).queue();
                log.error("Exception while handling slash command " + event, ex);
                ownerMessenger.messageOwner(msg);
            }
        });
    }
}
