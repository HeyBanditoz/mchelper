package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.http.DarkSkyClient;
import io.banditoz.mchelper.http.NominatimClient;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.DateUtils;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.darksky.DataItem;
import io.banditoz.mchelper.weather.geocoder.Location;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.DoubleSummaryStatistics;
import java.util.List;

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
        NominatimClient nominatimClient = ce.getMCHelper().getHttp().getNominatimClient();
        DarkSkyClient darkSkyClient = ce.getMCHelper().getHttp().getDarkSkyClient();

        List<Location> locs = nominatimClient.searchForLocation(ce.getCommandArgsString());
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

        ce.sendEmbedReply(new EmbedBuilder()
                .setTitle("Current Weather • " + response.currently().summary() + " • " + response.currently().temperature() + "°F",
                        "https://merrysky.net/forecast/" + response.latitude() + "," + response.longitude())
//                .setDescription(response.daily().summary())
                .addField("Temperature", response.currently().temperature() +
                        "°F (feels like " + response.currently().apparentTemperature() + "°F)", true)
                .addField("Low/High", stats.getMin() + "°F/" + stats.getMax() + "°F", true)
                .addField("Humidity", String.format("%.1f%%", response.currently().humidity() * 100), true)
                .addField("Wind", response.currently().windSpeed() + " mph", true)
                .addField("Precipitation", String.format("%.1f%%", response.currently().precipProbability() * 100), true)
                .addField("Pressure", String.format("%.1f mb\n%.3f atm", response.currently().pressure(), response.currently().pressure() / 1013.0), true)
                .setFooter(String.format("%s\n(%.5f,%.5f)", location.displayName(), response.latitude(), response.longitude()))
                .build());
        return Status.SUCCESS;
    }
}
