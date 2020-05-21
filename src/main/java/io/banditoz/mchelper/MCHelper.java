package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.quotes.QotdRunnable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MCHelper {
    private static JDA jda;
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false) // for reddit.app.link fetching
            .build(); // singleton http client
    private static final ObjectMapper OM = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER = LoggerFactory.getLogger(MCHelper.class);
    private static final ScheduledExecutorService SES = Executors.newScheduledThreadPool(1);
    private static final CommandHandler commandHandler = new CommandHandler();
    private static MessageCache CACHE;

    public static void setupBot() throws LoginException, InterruptedException {
        Settings settings = SettingsManager.getInstance().getSettings();
        if (settings.getDiscordToken() == null || settings.getDiscordToken().equals("Bot token here...")) {
            LOGGER.error("The Discord token is not configured correctly! The bot will now exit. Please check your Settings.json file.");
            System.exit(1);
        }
        Database.initializeDatabase(); // initialize the database first, so if something is wrong we'll exit
        jda = JDABuilder.createLight(settings.getDiscordToken()).enableIntents(GatewayIntent.GUILD_MEMBERS).build();
        jda.addEventListener(commandHandler);
        jda.addEventListener(new TeXListener());
        jda.addEventListener(new RedditListener());
        if (jda.getGatewayIntents().contains(GatewayIntent.GUILD_MEMBERS)) {
            jda.addEventListener(new GuildJoinLeaveListener());
        }
        else {
            LOGGER.info("GUILD_MEMBERS gateway intent not enabled. Not enabling the guild leave/join listener...");
        }
        if (settings.getWatchDeletedMessages()) {
            CACHE = new MessageCache(jda);
            jda.addEventListener(CACHE);
            jda.addEventListener(new DeletedMessageListener());
        }
        else {
            LOGGER.info("Message cache disabled.");
        }
        jda.awaitReady();

        // now that JDA is done loading, we can initialize things
        // that could have used it before initialization completes below.

        ReminderService.initialize();

        SES.scheduleAtFixedRate(new QotdRunnable(),
                QotdRunnable.getDelay().getSeconds(),
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);

        if (settings.getEsUrl() == null) {
            LOGGER.info("Elasticsearch URL not defined! Not showing temperature on status...");
        }
        else {
            Timer pingMeasurementTimer = new Timer();
            pingMeasurementTimer.schedule(new FahrenheitStatus(), 0L, TimeUnit.MINUTES.toMillis(1));
        }
    }

    public static ObjectMapper getObjectMapper() {
        return OM;
    }

    public static OkHttpClient getOkHttpClient() {
        return CLIENT;
    }

    public static JDA getJDA() {
        return jda;
    }

    public static MessageCache getMessageCache() {
        return CACHE;
    }

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }

    /**
     * Performs an HTTP request and returns the String. This is preferable to use over making your own
     * OkHttpClient or implementing one.
     *
     * @param request The Request object to use.
     * @return A String containing the body
     * @throws HttpResponseException If the response code is >=400
     * @throws IOException           If there was an issue performing the HTTP request
     * @see MCHelper#performHttpRequestGetResponse(Request)
     */
    public static String performHttpRequest(Request request) throws HttpResponseException, IOException {
        LOGGER.debug(request.toString());
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code());
        }
        String body = response.body().string();
        response.close();
        return body;
    }

    /**
     * Performs an HTTP request and returns the String. This is preferable to use over making your own
     * OkHttpClient or implementing one. <b>You must close the response body once you are done with it.</b>
     *
     * @param request The Request object to use.
     * @return A Response object. If you just need the response body, use MCHelper#performHttpRequest instead.
     * @throws HttpResponseException If the response code is >=400
     * @throws IOException           If there was an issue performing the HTTP request
     * @see MCHelper#performHttpRequest(Request)
     */
    public static Response performHttpRequestGetResponse(Request request) throws HttpResponseException, IOException {
        LOGGER.debug(request.toString());
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code());
        }
        return response;
    }
}
