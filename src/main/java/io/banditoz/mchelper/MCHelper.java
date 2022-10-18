package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.interactions.ButtonListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.lottery.LotteryManager;
import io.banditoz.mchelper.regexable.Regexable;
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public interface MCHelper {
    ObjectMapper getObjectMapper();
    JDA getJDA();
    Database getDatabase();
    ReminderService getReminderService();
    ThreadPoolExecutor getThreadPoolExecutor();
    Settings getSettings();
    CommandHandler getCommandHandler();
    List<Regexable> getRegexListeners();
    StatsRecorder getStatsRecorder();
    ScheduledExecutorService getSES();
//    RoleReactionListener getRRL();
    AccountManager getAccountManager();
    ButtonListener getButtonListener();
    GameManager getGameManager();
    LotteryManager getLotteryManager();
    User getOwner();

    void messageOwner(String s);
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
    String performHttpRequest(Request request) throws HttpResponseException, IOException;

    /**
     * Performs an HTTP request and returns nothing. This is preferable to use over making your own
     * OkHttpClient or implementing one.
     *
     * @param request The Request object to use.
     * @throws HttpResponseException If the response code is >=400
     * @throws IOException           If there was an issue performing the HTTP request.
     */
    void performHttpRequestIgnoreResponse(Request request) throws HttpResponseException, IOException;

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
    Response performHttpRequestGetResponse(Request request) throws HttpResponseException, IOException;
}
