package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.*;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import okhttp3.OkHttpClient;

import javax.security.auth.login.LoginException;

public class MCHelper {
    private static JDA jda;
    private static OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false) // for reddit.app.link fetching
            .build(); // singleton http client
    private static ObjectMapper om = new ObjectMapper();

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
    }

    public static ObjectMapper getObjectMapper() {
        return om;
    }

    public static OkHttpClient getOkHttpClient() {
        return client;
    }

    public static JDA getJDA() {
        return jda;
    }
}
