package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.interactions.ButtonListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.lottery.LotteryManager;
import io.banditoz.mchelper.regexable.Regexable;
import io.banditoz.mchelper.stats.StatsRecorder;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

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
    AccountManager getAccountManager();
    ButtonListener getButtonListener();
    GameManager getGameManager();
    LotteryManager getLotteryManager();
    User getOwner();
    void messageOwner(String s);
    Http getHttp();
    PollService getPollService();
}
