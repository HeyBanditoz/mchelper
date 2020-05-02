package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Region;
import io.banditoz.mchelper.commands.*;
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
    private static MessageCache CACHE;

    public static void setupBot() throws LoginException, InterruptedException {
        Settings settings = SettingsManager.getInstance().getSettings();
        if (settings.getDiscordToken() == null || settings.getDiscordToken().equals("Bot token here...")) {
            LOGGER.error("The Discord token is not configured correctly! The bot will now exit. Please check your Settings.json file.");
            System.exit(1);
        }
        Database.initializeDatabase(); // initialize the database first, so if something is wrong we'll exit
        jda = JDABuilder.createLight(settings.getDiscordToken()).enableIntents(GatewayIntent.GUILD_MEMBERS).build();
        LOGGER.info("Registering commands and listeners...");
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
        jda.addEventListener(new ReverseGeocoderCommand());
        jda.addEventListener(new DiceRollerCommand());
        jda.addEventListener(new CoinFlipCommand());
        jda.addEventListener(new VersionCommand());
        jda.addEventListener(new PingCommand());
        jda.addEventListener(new DefaultChannelCommand());
        jda.addEventListener(new PrefixCommand());
        jda.addEventListener(new HeapDumpCommand());
        jda.addEventListener(new UploadLogsCommand());
        jda.addEventListener(new FloodCommand());
        jda.addEventListener(new QuoteCommand());
        jda.addEventListener(new AddquoteCommand());
        jda.addEventListener(new SqlCommand());
        jda.addEventListener(new RemindmeCommand());
        jda.addEventListener(new DeleteReminderCommand());
        jda.addEventListener(new SnowflakeCommand());

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

        if (settings.getAlphaVantageKey() == null || settings.getAlphaVantageKey().equals("Alpha Vantage API key here")) {
            LOGGER.info("Alpha Vantage API key not defined! Not enabling financial commands.");
        }
        else {
            jda.addEventListener(new CurrencyConversionCommand());
            jda.addEventListener(new StockCommand());
        }

        if (settings.getRiotApiKey() == null || settings.getRiotApiKey().equals("Riot Api Key here")) {
            LOGGER.info("Riot API key not defined! Not enabling Orianna.");
        } else {
            Orianna.setRiotAPIKey(settings.getRiotApiKey());
            Orianna.setDefaultRegion(Region.NORTH_AMERICA);
            Thread thread = new Thread(LoadoutCommand::createData);
            thread.setName("Orianna");
            thread.start();
            jda.addEventListener(new LoadoutCommand());
        }
        jda.addEventListener(new HelpCommand()); // this must be registered last
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
