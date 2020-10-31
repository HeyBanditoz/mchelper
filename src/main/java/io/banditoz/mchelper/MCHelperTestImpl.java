package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.regexable.Regexable;
import io.banditoz.mchelper.regexable.RegexableHandler;
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.JDA;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class MCHelperTestImpl implements MCHelper {
    private final CommandHandler CH;
    private final RegexableHandler RH;

    public MCHelperTestImpl() {
        this.CH = new CommandHandler(this);
        this.RH = new RegexableHandler(this);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return null;
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
    public List<Command> getCommands() {
        return CH.getCommands();
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
    public String performHttpRequest(Request request) throws HttpResponseException, IOException {
        return null;
    }

    @Override
    public Response performHttpRequestGetResponse(Request request) throws HttpResponseException, IOException {
        return null;
    }

    private Settings getMockSettings() {
        Settings settings = new Settings();
        List<String> defaultOwners = new ArrayList<>();
        defaultOwners.add("12341234");
        settings.setDiscordToken("asdgfstherjuhgfj");
        settings.setBotOwners(defaultOwners);
        settings.setOwlBotToken("eryhue354uh3y4ewtgs");
        settings.setCommandThreads(1);
        settings.setWatchDeletedMessages(false);
        settings.setFinnhubKey("35uy7ewsgfhed");
        settings.setRiotApiKey("we98ytfghioned.");
        settings.setDatabaseName("no_database");
        settings.setDatabaseHostAndPort("localhost:3306");
        settings.setDatabaseUsername("root");
        settings.setDatabasePassword("toor");
        settings.setEsUrl("http://127.0.0.1");
        settings.setGrafanaToken("jsdfujw930tydsg");
        settings.setGrafanaUrl("http://127.0.0.1");
        return settings;
    }
}