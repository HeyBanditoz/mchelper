package io.banditoz.mchelper.commands;

import com.google.common.collect.Iterables;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.http.DarkSkyClient;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.weather.IconGenerator;
import io.banditoz.mchelper.weather.darksky.Currently;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.darksky.DataItem;
import io.banditoz.mchelper.weather.geocoder.Location;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static io.banditoz.mchelper.weather.TemperatureConverter.fToCHU;
import static java.lang.Math.round;

@Requires(config = "mchelper.darksky.token")
public class WeatherForecastCommand extends Command {
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-HH")
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault());

    @Override
    public String commandName() {
        return "wf";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<location>")
                .withDescription("Returns the weather forecast of a location.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) {
        DarkSkyClient darkSkyClient = ce.getMCHelper().getHttp().getDarkSkyClient();
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

        List<Location> locs = ce.getMCHelper().getNominatimLocationService().searchForLocation(locationToSearch);
        if (locs.isEmpty()) {
            ce.sendReply("Could not find location.");
            return Status.FAIL;
        }

        Location location = locs.get(0);
        DSWeather response = darkSkyClient.getForecast(location.lat(), location.lon());
        Currently c = response.currently();
        StringBuilder weather = new StringBuilder("When • Icon • Temp • Precip. Chance\n");
        for (List<DataItem> dataItems : Iterables.partition(response.hourly().data(), 3)) {
            DataItem data = DataItem.reduceSome(dataItems);
            weather.append(TimeFormat.DATE_TIME_SHORT.format(data.time()))
                    .append(" • ")
                    .append(IconGenerator.generateWeatherIcon(data.icon()))
                    .append(" • ")
                    .append("%d°F/%s°C".formatted(round(data.temperature()), fToCHU(data.temperature())))
                    .append(" • ")
                    .append("%d%%%s".formatted(round(data.precipProbability() * 100), data.getFormattedPrecipType()))
                    .append("\n");
        }
        ce.sendEmbedReply(new EmbedBuilder()
                .setTitle("Current Weather Forecast • %s • %d°F / %s°C".formatted(c.summary(), round(c.temperature()), fToCHU(c.temperature())),
                        "https://merrysky.net/forecast/%s,%s".formatted(response.latitude(), response.longitude()))
                .setDescription(weather.toString())
                .setFooter(String.format("%s\n(%.5f,%.5f)", location.displayName(), response.latitude(), response.longitude()))
                .build());
        return Status.SUCCESS;
    }
}
