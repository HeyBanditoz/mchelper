package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.avaje.config.Config;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.http.scraper.RssScraper;
import io.banditoz.mchelper.interactions.ButtonListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.lottery.LotteryManager;
import io.banditoz.mchelper.regexable.Regexable;
import io.banditoz.mchelper.regexable.RegexableHandler;
import io.banditoz.mchelper.runnables.PollCullerRunnable;
import io.banditoz.mchelper.runnables.QotdRunnable;
import io.banditoz.mchelper.runnables.UserMaintenanceRunnable;
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.weather.geocoder.NominatimLocationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.*;

public class MCHelperImpl implements MCHelper {
    private final JDA JDA;
    private final ObjectMapper OM = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Logger LOGGER = LoggerFactory.getLogger(MCHelperImpl.class);
    private final ScheduledExecutorService SES;
    private final ThreadPoolExecutor TPE;
    private final CommandHandler CH;
    private final RegexableHandler RH;
    private final ReminderService RS;
    private final Database DB;
    private final StatsRecorder STATS;
    private final AccountManager AM;
    private final ButtonListener BL;
    private final GameManager GM;
    private final LotteryManager LM;
    private final User OWNER;
    private final Http HTTP_HOLDER;
    private final PollService PS;
    private final NominatimLocationService NLS;
    private final RssScraper RSS_SCRAPER;
    private final ConfigurationProvider CONFIG_PROVIDER;
    private final OTel OTEL;

    public MCHelperImpl() throws InterruptedException {
        long before = System.currentTimeMillis();
        if (Files.exists(new File(".").toPath().resolve("Config.json"))) {
            LOGGER.warn("The old Config.json file still exists! Please migrate to application.yml. See the application-example.yml file in the Gitlab repo to migrate to. " +
                    "This warning will stop firing when Config.json no longer exists.");
        }
        if (Config.getNullable("mchelper.discord.token") == null) {
            LOGGER.error("The Discord token is not configured correctly! The bot will now exit. Please check your application.yml file.");
            System.exit(1);
        }

        // Splash
        LOGGER.info("___  ________  _   _      _                 ");
        LOGGER.info("|  \\/  /  __ \\| | | |    | |                ");
        LOGGER.info("| .  . | /  \\/| |_| | ___| |_ __   ___ _ __ ");
        LOGGER.info("| |\\/| | |    |  _  |/ _ \\ | '_ \\ / _ \\ '__|");
        LOGGER.info("| |  | | \\__/\\| | | |  __/ | |_) |  __/ |   ");
        LOGGER.info("\\_|  |_/\\____/\\_| |_/\\___|_| .__/ \\___|_|   ");
        LOGGER.info("                           | |              ");
        LOGGER.info("                           |_|              ");
        LOGGER.info("MCHelper version {} using JDA {} running on JVM {} committed on {}", Version.GIT_SHA, JDAInfo.VERSION, Runtime.version(), Version.GIT_DATE);

        TPE = new ThreadPoolExecutor(Config.getInt("mchelper.command-threads"), Config.getInt("mchelper.command-threads"),
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("Command-%d").build());
        SES = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Scheduled-%d")
                .build());
        GM = new GameManager();
        JDA = buildJDA();

        if (Config.getBool("mchelper.metrics.enabled", false)) {
            OTEL = new OTel(true);
            JDA.addEventListener(new MetricsEventListener(OTEL.meter()));
        } else {
            LOGGER.info("Not enabling metrics.");
            OTEL = new OTel(false);
        }
        STATS = new StatsRecorder(this, TPE);

        // Shut things down gracefully.

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        JDA.awaitReady();
        // now that JDA is done loading, we can initialize things
        // that could have used it before initialization completed.
        if (Database.isConfigured()) {
            DB = new Database(getOTel().openTelemetry());
            RS = new ReminderService(this, SES);
            AM = new AccountManager(DB);
            LM = new LotteryManager(this);
            PS = new PollService(this);
            JDA.addEventListener(new RoleReactionListener(this));
            UserMaintenanceRunnable userMaintenanceRunnable = new UserMaintenanceRunnable(this);
            PollCullerRunnable pollCullerRunnable = new PollCullerRunnable(this);
            SES.scheduleWithFixedDelay(userMaintenanceRunnable,10, 43200, TimeUnit.SECONDS);
            SES.scheduleWithFixedDelay(pollCullerRunnable, 120, 86400, TimeUnit.SECONDS);
            GuildVoiceListener voiceListener = new GuildVoiceListener(this);
            voiceListener.updateAll();
            JDA.addEventListener(voiceListener);
        }
        else {
            DB = null;
            RS = null;
            AM = null;
            LM = null;
            PS = null;
            LOGGER.warn("The database is not configured! All database functionality will not be enabled.");
        }
        CONFIG_PROVIDER = new ConfigurationProvider(this);
        // TODO GuildJoinLeaveListener wants ConfigurationProvider during db init but we have to init ConfigProvider after DB init... fix the weirdness there
        if (Database.isConfigured()) {
            if (JDA.getGatewayIntents().contains(GatewayIntent.GUILD_MEMBERS)) {
                JDA.addEventListener(new GuildJoinLeaveListener(this));
            }
            else {
                LOGGER.info("GUILD_MEMBERS gateway intent not enabled. Not enabling the guild leave/join listener...");
            }
        }
        this.CH = buildCommandHandler();
        this.RH = buildRegexableHandler();
        JDA.addEventListener(CH, RH);
        BL = new ButtonListener(this);

        // deterministic order for onButtonInteraction handling, and all in one thread
        // TODO clean this shit up!
        JDA.addEventListener(new ListenerAdapter() {
            @Override
            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                TPE.execute(() -> {
                    if (PS != null) {
                        PS.onButtonInteraction(event);
                    }
                    if (BL != null) {
                        BL.onButtonInteraction(event);
                    }
//                    if (!event.isAcknowledged()) {
//                        event.reply("""
//                                Your button interaction was not acknowledged by any listener.
//                                It most likely doesn't exist anymore. (i.e. bot restart.)
//                                If this is a poll, try again in a minute.""").setEphemeral(true).queue();
//                    }
                });
            }

            @Override
            public void onMessageDelete(@NotNull MessageDeleteEvent event) {
                TPE.execute(() -> {
                    if (PS != null) {
                        try {
                            PS.disablePollsByMessageId(List.of(event.getMessageIdLong()));
                        } catch (SQLException e) {
                            LOGGER.warn("Could not disable poll.", e);
                        }
                    }
                });
            }

            @Override
            public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
                TPE.execute(() -> {
                    if (PS != null) {
                        try {
                            PS.disablePollsByMessageId(event.getMessageIds().stream().map(Long::valueOf).toList());
                        } catch (SQLException e) {
                            LOGGER.warn("Could not disable poll.", e);
                        }
                    }
                });
                super.onMessageBulkDelete(event);
            }
        });

        JDA.addEventListener(new SelfGuildJoinLeaveListener());
        JDA.addEventListener(new FileUploadListener(this));

        // I just want patch notes, not twitch advertisements
        if (System.getenv("TARKOV_CHANNEL") != null) {
            LOGGER.info("Deleting messages from " + System.getenv("TARKOV_CHANNEL") + " which contain twitch.tv links...");
            JDA.addEventListener(new AntiTwitch());
        }

        SES.scheduleAtFixedRate(new QotdRunnable(this),
                QotdRunnable.getDelay().getSeconds(),
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);

        OWNER = JDA.retrieveApplicationInfo().complete().getOwner();
        HTTP_HOLDER = new Http(this);
        NLS = new NominatimLocationService(HTTP_HOLDER.getNominatimClient());
        RSS_SCRAPER = new RssScraper();

        LOGGER.info("MCHelper initialization finished in {} seconds.", new DecimalFormat("#.#").format((System.currentTimeMillis() - before) / 1000D));
    }

    private void shutdown() {
        LOGGER.info("Shutdown hook fired. Denying new commands, and waiting for all interactables to close...");
        CH.dontAcceptNewCommands();
        long then = System.currentTimeMillis();
        int activeInteractables;
        while ((activeInteractables = BL.getActiveInteractables()) != 0) {
            LOGGER.info("Waited {} ms for {} interactables to finish...", (System.currentTimeMillis() - then), activeInteractables);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Interactables finished. Closing DB pool...");
        if (DB != null) DB.close();
        LOGGER.info("DB pool closed. Shutting down JDA and thread pools...");
        if (SES != null) SES.shutdown();
        if (TPE != null) TPE.shutdown();
        if (JDA != null) JDA.shutdown();
        LOGGER.info("Shutdown complete. Goodbye.");
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return OM;
    }

    @Override
    public JDA getJDA() {
        return JDA;
    }

    @Override
    public Database getDatabase() {
        return DB;
    }

    @Override
    public ReminderService getReminderService() {
        return RS;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return TPE;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return CH;
    }

    @Override
    public List<Regexable> getRegexListeners() {
        return RH.getRegexables();
    }

    @Override
    public StatsRecorder getStatsRecorder() {
        return STATS;
    }

    @Override
    public ScheduledExecutorService getSES() {
        return SES;
    }

    @Override
    public AccountManager getAccountManager() {
        return AM;
    }

    @Override
    public LotteryManager getLotteryManager() {
        return LM;
    }

    @Override
    public ButtonListener getButtonListener() {
        return BL;
    }

    @Override
    public GameManager getGameManager() {
        return GM;
    }

    @Override
    public User getOwner() {
        return OWNER;
    }

    public Http getHttp() {
        return HTTP_HOLDER;
    }

    @Override
    public PollService getPollService() {
        return PS;
    }

    @Override
    public NominatimLocationService getNominatimLocationService() {
        return NLS;
    }

    @Override
    public RssScraper getRssScraper() {
        return RSS_SCRAPER;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return CONFIG_PROVIDER;
    }

    @Override
    public OTel getOTel() {
        return OTEL;
    }

    /**
     * Builds JDA. It's in its own method so the program can quit if it can't contact Discord
     * (so systemd can restart it until it can.)
     *
     * @return A JDA instance.
     */
    private JDA buildJDA() {
        try {
            return JDABuilder.createDefault(Config.get("mchelper.discord.token"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMaxReconnectDelay(32)
                    .setEnableShutdownHook(false)
                    .build();
        } catch (Exception ex) {
            LOGGER.error("Couldn't build JDA. Exiting!", ex);
            shutdown();
            System.exit(1);
        }
        return null;
    }

    private CommandHandler buildCommandHandler() {
        try {
            return new CommandHandler(this);
        } catch (Exception ex) {
            LOGGER.error("Couldn't build the command handler. Exiting!", ex);
            shutdown();
            System.exit(1);
        }
        return null;
    }

    private RegexableHandler buildRegexableHandler() {
        try {
            return new RegexableHandler(this);
        } catch (Exception ex) {
            LOGGER.error("Couldn't build the regexable handler. Exiting!", ex);
            shutdown();
            System.exit(1);
        }
        return null;
    }

    @Override
    public void messageOwner(String s) {
        JDA.openPrivateChannelById(getOwner().getIdLong())
                .flatMap(channel -> channel.sendMessage(s))
                .queue(message -> {}, throwable -> LOGGER.error("Could not message the owner {}!", getOwner(), throwable));
    }
}
