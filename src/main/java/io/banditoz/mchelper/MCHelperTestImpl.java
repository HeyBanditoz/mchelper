package io.banditoz.mchelper;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.http.scraper.RssScraper;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.llm.LLMService;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.lottery.LotteryManager;
import io.banditoz.mchelper.mtg.ScryfallService;
import io.banditoz.mchelper.regexable.Regexable;
import io.banditoz.mchelper.regexable.RegexableHandler;
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.weather.WeatherService;
import io.banditoz.mchelper.weather.geocoder.NominatimLocationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

// This class only exists so CommandsToMarkdown can run, as Mockito is only used for testing.
public class MCHelperTestImpl implements MCHelper {
    private final CommandHandler CH;
    private final RegexableHandler RH;
    private final ObjectMapper OM;
    private final Http HTTP_HOLDER;
    private final NominatimLocationService NLS;

    public MCHelperTestImpl() throws Exception {
        this.OM = new ObjectMapper().registerModule(new JavaTimeModule());
        this.CH = new CommandHandler(this);
        this.RH = new RegexableHandler(this);
        this.HTTP_HOLDER = new Http(this.getObjectMapper());
        this.NLS = new NominatimLocationService(HTTP_HOLDER.getNominatimClient());
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
    public InteractionListener getInteractionListener() {
        return null;
    }

    @Override
    public GameManager getGameManager() {
        return null;
    }

    @Override
    public LotteryManager getLotteryManager() {
        return null;
    }

    @Override
    public User getOwner() {
        return null;
    }

    @Override
    public Http getHttp() {
        return HTTP_HOLDER;
    }

    @Override
    public PollService getPollService() {
        return null;
    }

    @Override
    public NominatimLocationService getNominatimLocationService() {
        return NLS;
    }

    @Override
    public RssScraper getRssScraper() {
        return null;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return null;
    }

    @Override
    public OTel getOTel() {
        return null;
    }

    @Override
    public LLMService getLLMService() {
        return null;
    }

    @Override
    public WeatherService getWeatherService() {
        return null;
    }

    @Override
    public ScryfallService getScryfallService() {
        return null;
    }

    @Override
    public void messageOwner(String s) {}
}
