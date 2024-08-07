package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.DateUtils;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.weather.ForecastSummary;
import io.banditoz.mchelper.weather.darksky.Currently;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.darksky.DataItem;
import io.banditoz.mchelper.weather.geocoder.Location;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.DoubleSummaryStatistics;

import static io.banditoz.mchelper.weather.TemperatureConverter.fToCHU;
import static java.lang.Math.round;

@Requires(config = "mchelper.darksky.token")
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
        String locationToSearch;
        if (ce.getCommandArgsString().isBlank()) {
            String configuredLocation = ce.getConfig().get(Config.WEATHER_DEFAULT_LOC);
            if (configuredLocation == null) {
                ce.sendReply("No location provided, and no Config.WEATHER_DEFAULT_LOC was specified.\n" +
                        "You can use this command to set a default: !config WEATHER_DEFAULT_LOC location here");
                return Status.FAIL;
            }
            else {
                locationToSearch = configuredLocation;
            }
        }
        else {
            locationToSearch = ce.getCommandArgsString();
        }

        ForecastSummary forecast = ce.getMCHelper().getWeatherService().getForecastForLocation(locationToSearch);
        DSWeather response = forecast.forecast();
        Location location = forecast.location();

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
                .addField("Precipitation", "%s%%%s".formatted(round(c.precipProbability() * 100D), c.getFormattedPrecipType()), true)
                .addField("Pressure", "%s mb\n%.3f atm".formatted(round(c.pressure()), c.pressure() / 1013.0), true)
                .setFooter("%s\n(%.4f,%.4f)".formatted(location.displayName(), response.latitude(), response.longitude()))
                .build());
        return Status.SUCCESS;
    }
}
