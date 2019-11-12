package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.weather.es.EsUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class FahrenheitStatus extends TimerTask {
    // TODO Use ScheduledExecutorService instead
    @Override
    public void run() {
        JDA jda = MCHelper.getJDA();
        try {
            double f = EsUtils.getFahrenheit();
            Activity a = Activity.playing(f + "°F");
            jda.getPresence().setActivity(a);
        }
        catch (Exception ex) {
            Activity a = Activity.playing("Error!");
            jda.getPresence().setActivity(a);
            if (!(SettingsManager.getInstance().getSettings().getEsUrl() == null)) {
                LoggerFactory.getLogger(FahrenheitStatus.class).error("Error on getting temperature!", ex);
            }
        }
    }
}
