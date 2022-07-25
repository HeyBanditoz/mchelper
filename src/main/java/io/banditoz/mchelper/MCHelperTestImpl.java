package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.interactions.ButtonListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.regexable.Regexable;
import io.banditoz.mchelper.regexable.RegexableHandler;
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

// This class only exists so CommandsToMarkdown can run, as Mockito is only used for testing.
public class MCHelperTestImpl implements MCHelper {
    private final CommandHandler CH;
    private final RegexableHandler RH;
    private final ObjectMapper OM;
    private final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false) // for reddit.app.link fetching
            .build(); // singleton http client

    public MCHelperTestImpl() throws Exception {
        this.OM = new ObjectMapper().registerModule(new JavaTimeModule());
        this.CH = new CommandHandler(this);
        this.RH = new RegexableHandler(this);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return OM;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public Database getDatabase() {
        return null;
    }

    @Override
    public ReminderService getReminderService() {
        return null;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return null;
    }

    @Override
    public Settings getSettings() {
        return getMockSettings();
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
        return null;
    }

    @Override
    public ScheduledExecutorService getSES() {
        return null;
    }

    @Override
    public AccountManager getAccountManager() {
        return null;
    }

    @Override
    public ButtonListener getButtonListener() {
        return null;
    }

    @Override
    public GameManager getGameManager() {
        return null;
    }

    @Override
    public User getOwner() {
        return null;
    }

    @Override
    public void messageOwner(String s) {}

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
        request = request.newBuilder().addHeader("User-Agent", "MCHelper/" + Version.GIT_SHA + " okhttp/" + OkHttp.VERSION + " (+https://gitlab.com/HeyBanditoz/mchelper)").build();
        Response response = CLIENT.newCall(request).execute();
        if (response.code() >= 400) {
            response.close();
            throw new HttpResponseException(response.code());
        }
        return response;
    }


    private Settings getMockSettings() {
        Settings settings = new Settings();
        List<String> defaultOwners = new ArrayList<>();
        defaultOwners.add("12341234");
        settings.setDiscordToken("asdgfstherjuhgfj");
        settings.setBotOwners(defaultOwners);
        settings.setOwlBotToken("eryhue354uh3y4ewtgs");
        settings.setCommandThreads(1);
        settings.setFinnhubKey("35uy7ewsgfhed");
        settings.setRiotApiKey("we98ytfghioned.");
        settings.setTarkovMarketApiKey("sgfjklw4epoitju");
        settings.setTarkovToolsApiEndpoint("https://api.tarkov.dev/graphql");
        return settings;
    }
}
