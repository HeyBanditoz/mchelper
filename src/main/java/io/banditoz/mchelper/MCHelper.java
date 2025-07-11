package io.banditoz.mchelper;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.weather.WeatherService;
import io.banditoz.mchelper.weather.geocoder.NominatimLocationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public interface MCHelper {
    ObjectMapper getObjectMapper();
    JDA getJDA();
    Database getDatabase();
    ReminderService getReminderService();
    ThreadPoolExecutor getThreadPoolExecutor();
    CommandHandler getCommandHandler();
    List<Regexable> getRegexListeners();
    StatsRecorder getStatsRecorder();
    ScheduledExecutorService getSES();
    AccountManager getAccountManager();
    InteractionListener getInteractionListener();
    GameManager getGameManager();
    LotteryManager getLotteryManager();
    User getOwner();
    void messageOwner(String s);
    Http getHttp();
    PollService getPollService();
    NominatimLocationService getNominatimLocationService();
    RssScraper getRssScraper();
    ConfigurationProvider getConfigurationProvider();
    OTel getOTel();
    LLMService getLLMService();
    WeatherService getWeatherService();
    ScryfallService getScryfallService();
}
