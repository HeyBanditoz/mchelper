package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.http.DarkSkyClient;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.DateUtils;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.weather.darksky.Currently;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.darksky.DataItem;
import io.banditoz.mchelper.weather.geocoder.Location;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import static io.banditoz.mchelper.weather.TemperatureConverter.fToCHU;
import static java.lang.Math.round;

@Requires(settingsMethod = "getDarkSkyApiKey")
public class WeatherCommand extends Command {
    @Override
    public String commandName() {
        return "w";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<location>")
                .withDescription("Returns the current weather of a location.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        DarkSkyClient darkSkyClient = ce.getMCHelper().getHttp().getDarkSkyClient();

        List<Location> locs = ce.getMCHelper().getNominatimLocationService().searchForLocation(ce.getCommandArgsString());
        if (locs.isEmpty()) {
            ce.sendReply("Could not find location.");
            return Status.FAIL;
        }

        Location location = locs.get(0);
        DSWeather response = darkSkyClient.getForecast(location.lat(), location.lon());
        Instant nextMidnight = DateUtils.getNextMidnight();
        Instant morning = DateUtils.getNextMidnight().minus(1, ChronoUnit.DAYS);
        DoubleSummaryStatistics stats = response.hourly().data().stream()
                .filter(di -> !di.time().isAfter(morning) || di.time().isAfter(nextMidnight))
                .mapToDouble(DataItem::temperature)
                .summaryStatistics();
        
        Currently c = response.currently();
        
        ce.sendEmbedReply(new EmbedBuilder()
                .setTitle("Current Weather • %s • %d°F / %s°C".formatted(c.summary(), round(c.temperature()), fToCHU(c.temperature())),
                        "https://merrysky.net/forecast/%s,%s".formatted(response.latitude(), response.longitude()))
//                .setDescription(response.daily().summary())
                .addField("Temperature", "%s°F/%s°C\n(feels like %s°F/%s°C)"
                        .formatted(round(c.temperature()), fToCHU(c.temperature()), round(c.apparentTemperature()), fToCHU(c.apparentTemperature())), true)
                .addField("Low/High", "%s°F/%s°F\n%s°C/%s°C"
                        .formatted(round(stats.getMin()), round(stats.getMax()), fToCHU(stats.getMin()), fToCHU(stats.getMax())), true)
                .addField("Humidity", "%s%%".formatted(round(c.humidity() * 100D)), true)
                .addField("Wind", "%s mph".formatted(round(c.windSpeed())), true)
                .addField("Precipitation", "%s%%".formatted(round(c.precipProbability() * 100D)), true)
                .addField("Pressure", "%s mb\n%.3f atm".formatted(round(c.pressure()), c.pressure() / 1013.0), true)
                .setFooter("%s\n(%.4f,%.4f)".formatted(location.displayName(), response.latitude(), response.longitude()))
                .build());
        return Status.SUCCESS;
    }
}
