package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.weather.es.Weather;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.LoggerFactory;

public class FahrenheitStatus implements Runnable {
    private final MCHelper MCHELPER;

    public FahrenheitStatus(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    @Override
    public void run() {
        try {
            double f = new Weather(MCHELPER).getFahrenheit();
            Activity a = Activity.playing(f + "°F");
            MCHELPER.getJDA().getPresence().setActivity(a);
        } catch (Exception ex) {
            Activity a = Activity.playing("Error!");
            MCHELPER.getJDA().getPresence().setActivity(a);
            LoggerFactory.getLogger(FahrenheitStatus.class).error("Error on getting temperature!", ex);
        }
    }
}
