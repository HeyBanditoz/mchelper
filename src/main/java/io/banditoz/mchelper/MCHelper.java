package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.*;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class MCHelper {
    private static JDA jda;
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false) // for reddit.app.link fetching
            .build(); // singleton http client
    private static final ObjectMapper OM = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(MCHelper.class);
    private static MessageCache CACHE;

    public static void setupBot() throws LoginException, InterruptedException {
        Settings settings = SettingsManager.getInstance().getSettings();
        if (settings.getDiscordToken() == null || settings.getDiscordToken().equals("Bot token here...")) {
            LOGGER.error("The Discord token is not configured correctly! The bot will now exit. Please check your Settings.json file.");
            System.exit(1);
        }
        Database.getInstance(); // initialize the database first, so if something is wrong we'll exit
        jda = new JDABuilder(settings.getDiscordToken())
                .setDisabledCacheFlags(EnumSet.allOf(CacheFlag.class)) // we don't use (activity, voice state, emote, client status) so disable their caches.
                .build();
        jda.awaitReady();
        jda.addEventListener(new DeletedMessageListener());
        jda.addEventListener(new GuildJoinLeaveListener());
        jda.addEventListener(new BashCommand());
        jda.addEventListener(new InfoCommand());
        jda.addEventListener(new MathCommand());
        jda.addEventListener(new EangleCommand());
        jda.addEventListener(new NetherCommand());
        jda.addEventListener(new OverworldCommand());
        jda.addEventListener(new UnitsCommand());
        jda.addEventListener(new CoordCommand());
        jda.addEventListener(new TeXCommand());
        jda.addEventListener(new TeXListener());
        jda.addEventListener(new RedditListener());
        jda.addEventListener(new PickCommand());
        jda.addEventListener(new ToMorseCommand());
        jda.addEventListener(new FromMorseCommand());
        jda.addEventListener(new EvalCommand());
        jda.addEventListener(new TTTCommand());
        jda.addEventListener(new ReverseGeocoderCommand());
        jda.addEventListener(new DiceRollerCommand());
        jda.addEventListener(new CoinFlipCommand());
        jda.addEventListener(new VersionCommand());
        jda.addEventListener(new PingCommand());
        jda.addEventListener(new DefaultChannelCommand());
        jda.addEventListener(new PrefixCommand());
        jda.addEventListener(new HeapDumpCommand());
        if (settings.getWatchDeletedMessages()) {
            CACHE = new MessageCache(jda);
            jda.addEventListener(CACHE);
        }
        else {
            LOGGER.info("Message cache disabled.");
        }

        if (settings.getOwlBotToken() == null || settings.getOwlBotToken().equals("OwlBot API key here.")) {
            LOGGER.info("No OwlBot API key defined! Not enabling the dictionary define command...");
        }
        else {
            jda.addEventListener(new DictionaryCommand());
        }

        if (settings.getDarkSkyAPI() == null || settings.getDarkSkyAPI().equals("Dark Sky API key here.")) {
            LOGGER.info("No dark sky API key defined! Not enabling the weather command...");
        }
        else {
            jda.addEventListener(new WeatherCommand());
            jda.addEventListener(new WeatherForecastCommand());
        }

        if (settings.getEsUrl() == null || settings.getGrafanaToken() == null || settings.getGrafanaUrl() == null) {
            LOGGER.info("No weather station configs defined! Not enabling the weather station command...");
        }
        else {
            jda.addEventListener(new WeatherStationCommand());
        }

        if (settings.getEsUrl() == null) {
            LOGGER.info("Elasticsearch URL not defined! Not showing temperature on status...");
        }
        else {
            Timer pingMeasurementTimer = new Timer();
            pingMeasurementTimer.schedule(new FahrenheitStatus(), 0L, TimeUnit.MINUTES.toMillis(1));
        }
        jda.addEventListener(new HelpCommand()); // this must be registered last
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

    /**
     * Performs an HTTP request and returns the String. This is preferable to use over making your own
     * OkHttpClient or implementing one.
     *
     * @param request The Request object to use.
     * @return A String containing the body
     * @throws HttpResponseException If the response code is >=400
     * @throws IOException If there was an issue performing the HTTP request
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
     * @throws IOException If there was an issue performing the HTTP request
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
