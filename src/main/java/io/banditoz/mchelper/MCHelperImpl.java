package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.quotes.QotdRunnable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
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
    private final ReminderService RS;
    private final Database DB;
    private final Settings SETTINGS;

    public MCHelperImpl() throws LoginException, InterruptedException {
        this.SETTINGS = new SettingsManager(new File(".").toPath().resolve("Config.json")).getSettings(); // TODO Make config file location configurable via program arguments
        TPE = new ThreadPoolExecutor(SETTINGS.getCommandThreads(), SETTINGS.getCommandThreads(),
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("Command-%d").build());
        SES = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Scheduled-%d")
                .build());
        this.CH = new CommandHandler(this);

        if (SETTINGS.getDiscordToken() == null || SETTINGS.getDiscordToken().equals("Bot token here...")) {
            LOGGER.error("The Discord token is not configured correctly! The bot will now exit. Please check your Config.json file.");
            System.exit(1);
        }

        JDA = JDABuilder.createLight(SETTINGS.getDiscordToken()).enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        JDA.addEventListener(CH);
        JDA.addEventListener(new TeXListener(this));
        JDA.addEventListener(new RedditListener(this));

        DB = new Database(this);

        if (JDA.getGatewayIntents().contains(GatewayIntent.GUILD_MEMBERS)) {
            JDA.addEventListener(new GuildJoinLeaveListener(this));
        }
        else {
            LOGGER.info("GUILD_MEMBERS gateway intent not enabled. Not enabling the guild leave/join listener...");
        }

        JDA.awaitReady();

        // now that JDA is done loading, we can initialize things
        // that could have used it before initialization completes below.

        RS = new ReminderService(this, SES);
        SES.scheduleAtFixedRate(new QotdRunnable(this),
                QotdRunnable.getDelay().getSeconds(),
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);

        if (SETTINGS.getEsUrl() == null) {
            LOGGER.info("Elasticsearch URL not defined! Not showing temperature on status...");
        }
        else {
            SES.scheduleAtFixedRate(new FahrenheitStatus(this), 0L, 1, TimeUnit.MINUTES);
        }
        LOGGER.info("MCHelper initialization finished.");
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
    public List<Command> getCommands() {
        return CH.getCommands();
    }

    @Override
    public String performHttpRequest(Request request) throws HttpResponseException, IOException {
        LOGGER.debug(request.toString());
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code());
        }
        String body = response.body().string();
        response.close();
        return body;
    }

    @Override
    public Response performHttpRequestGetResponse(Request request) throws HttpResponseException, IOException {
        LOGGER.debug(request.toString());
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code());
        }
        return response;
    }
}
