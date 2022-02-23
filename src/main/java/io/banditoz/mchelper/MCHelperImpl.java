package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.interactions.ButtonListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.regexable.Regexable;
import io.banditoz.mchelper.regexable.RegexableHandler;
import io.banditoz.mchelper.runnables.QotdRunnable;
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.RoleReactionListener;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.UserMaintenanceRunnable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class MCHelperImpl implements MCHelper {
    private final JDA JDA;
    private final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false) // for reddit.app.link fetching
            .build(); // singleton http client
    private final ObjectMapper OM = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Logger LOGGER = LoggerFactory.getLogger(MCHelperImpl.class);
    private final ScheduledExecutorService SES;
    private final ThreadPoolExecutor TPE;
    private final CommandHandler CH;
    private final RegexableHandler RH;
    private final ReminderService RS;
    private final Database DB;
    private final Settings SETTINGS;
    private final StatsRecorder STATS;
    private final RoleReactionListener RRL;
    private final AccountManager AM;
    private final ButtonListener BL;

    public MCHelperImpl() throws InterruptedException {
        this.SETTINGS = new SettingsManager(new File(".").toPath().resolve("Config.json")).getSettings(); // TODO Make config file location configurable via program arguments

        if (SETTINGS.getDiscordToken() == null || SETTINGS.getDiscordToken().equals("Bot token here...")) {
            LOGGER.error("The Discord token is not configured correctly! The bot will now exit. Please check your Config.json file.");
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
        LOGGER.info("MCHelper version " + Version.GIT_SHA + " committed on " + Version.GIT_DATE);

        TPE = new ThreadPoolExecutor(SETTINGS.getCommandThreads(), SETTINGS.getCommandThreads(),
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("Command-%d").build());
        SES = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Scheduled-%d")
                .build());
        JDA = buildJDA();

        if (SETTINGS.getElasticsearchMessageEndpoint() != null && !SETTINGS.getElasticsearchMessageEndpoint().equals("http://endpoint:9200/thing/_doc")) {
            LOGGER.info("Enabling the Elasticsearch message logging service for the following channels: " + SETTINGS.getLoggedChannels());
            JDA.addEventListener(new MessageLogger(this, TPE));
        }

        STATS = new StatsRecorder(this, TPE);

        // Shut things down gracefully.

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        JDA.awaitReady();
        // now that JDA is done loading, we can initialize things
        // that could have used it before initialization completed.
        if (Database.isConfigured()) {
            DB = new Database();
            RS = new ReminderService(this, SES);
            AM = new AccountManager(DB);
            RRL = new RoleReactionListener(this);
            JDA.addEventListener(RRL.getAddHandler(), RRL.getRemoveHandler());
            SES.scheduleWithFixedDelay(new UserMaintenanceRunnable(this),
                    10,
                    43200,
                    TimeUnit.SECONDS);
            if (JDA.getGatewayIntents().contains(GatewayIntent.GUILD_MEMBERS)) {
                JDA.addEventListener(new GuildJoinLeaveListener(DB));
            }
            else {
                LOGGER.info("GUILD_MEMBERS gateway intent not enabled. Not enabling the guild leave/join listener...");
            }

        }
        else {
            DB = null;
            RS = null;
            AM = null;
            RRL = null;
            LOGGER.warn("The database is not configured! All database functionality will not be enabled.");
        }
        this.CH = buildCommandHandler();
        this.RH = buildRegexableHandler();
        JDA.addEventListener(CH, RH);
        BL = new ButtonListener(this);
        JDA.addEventListener(BL);
        JDA.addEventListener(new SelfGuildJoinLeaveListener());
        JDA.addEventListener(new FileUploadListener(this));

        SES.scheduleAtFixedRate(new QotdRunnable(this),
                QotdRunnable.getDelay().getSeconds(),
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);

        LOGGER.info("MCHelper initialization finished.");
    }

    private void shutdown() {
        LOGGER.info("Shutdown hook fired. Shutting down JDA and thread pools...");
        if (SES != null) SES.shutdown();
        if (TPE != null) TPE.shutdown();
        if (JDA != null) JDA.shutdown();
        if (getDatabase() != null) getDatabase().close();
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
    public Settings getSettings() {
        return SETTINGS;
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
    public RoleReactionListener getRRL() {
        return RRL;
    }

    @Override
    public ButtonListener getButtonListener() {
        return BL;
    }

    /**
     * Builds JDA. It's in its own method so the program can quit if it can't contact Discord
     * (so systemd can restart it until it can.)
     *
     * @return A JDA instance.
     */
    private JDA buildJDA() {
        try {
            return JDABuilder.createDefault(SETTINGS.getDiscordToken())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.VOICE_STATE)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMaxReconnectDelay(32)
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
    public String performHttpRequest(Request request) throws HttpResponseException, IOException {
        String s;
        try (Response r = placeRequest(request)) {
            s = r.body().string();
        }
        return s;
    }

    @Override
    public Response performHttpRequestGetResponse(Request request) throws HttpResponseException, IOException {
        try (Response r = placeRequest(request)) {
            return r;
        }
    }

    @Override
    public void performHttpRequestIgnoreResponse(Request request) throws HttpResponseException, IOException {
        try (Response ignored = placeRequest(request)) {
        }
    }

    private Response placeRequest(Request request) throws HttpResponseException, IOException {
        LOGGER.debug(request.toString());
        request = request.newBuilder().addHeader("User-Agent", "MCHelper/" + Version.GIT_SHA + ' ' + okhttp3.internal.Version.userAgent() + " (+https://gitlab.com/HeyBanditoz/mchelper)").build();
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            response.close();
            throw new HttpResponseException(response.code());
        }
        return response;
    }
}
