package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.*;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
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

    public static void setupBot() throws LoginException, InterruptedException {
        Settings settings = SettingsManager.getInstance().getSettings();
        jda = new JDABuilder(settings.getDiscordToken()).build();
        jda.awaitReady();
        jda.addEventListener(new BashCommand());
        jda.addEventListener(new InfoCommand());
        jda.addEventListener(new EvalCommand());
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
        jda.addEventListener(new JSEvalCommand());
        jda.addEventListener(new WeatherCommand());
        jda.addEventListener(new DictionaryCommand());
	jda.addEventListener(new TTT());
        if (settings.getEsUrl() == null || settings.getGrafanaToken() == null || settings.getGrafanaUrl() == null) {
            LOGGER.warn("No weather station configs defined! Not enabling the weather station command...");
        }
        else {
            jda.addEventListener(new WeatherStationCommand());
        }
        jda.addEventListener(new ReverseGeocoderCommand());
        if (!(settings.getEsUrl() == null)) {
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

    public static String performHttpRequest(Request request) throws HttpResponseException, IOException {
        LOGGER.debug(request.toString());
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code());
        }
        return response.body().string();
    }

    public static Response performHttpRequestGetResponse(Request request) throws HttpResponseException, IOException {
        LOGGER.debug(request.toString());
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code());
        }
        return response;
    }
}
