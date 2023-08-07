package io.banditoz.mchelper.commands;

import com.google.common.collect.Iterables;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.http.DarkSkyClient;
import io.banditoz.mchelper.http.NominatimClient;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.weather.IconGenerator;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.darksky.DataItem;
import io.banditoz.mchelper.weather.geocoder.Location;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
        NominatimClient nominatimClient = ce.getMCHelper().getHttp().getNominatimClient();
        DarkSkyClient darkSkyClient = ce.getMCHelper().getHttp().getDarkSkyClient();

        List<Location> locs = nominatimClient.searchForLocation(ce.getCommandArgsString());
        if (locs.isEmpty()) {
            ce.sendReply("Could not find location.");
            return Status.FAIL;
        }

        Location location = locs.get(0);
        DSWeather response = darkSkyClient.getForecast(location.lat(), location.lon());
        StringBuilder weather = new StringBuilder("Date-Time • Icon • Temp • Precip. Chance\n");
        for (List<DataItem> dataItems : Iterables.partition(response.hourly().data(), 3)) {
            DataItem data = dataItems.get(0);
            if (data == null) {
                continue;
            }
            weather.append(formatter.format(data.time()))
                    .append(" • ")
                    .append(IconGenerator.generateWeatherIcon(data.icon()))
                    .append(" • ")
                    .append(data.temperature())
                    .append("°F • ")
                    .append(String.format("%.1f%%", response.currently().precipProbability() * 100))
                    .append("\n");
        }
        ce.sendEmbedReply(new EmbedBuilder()
                .setTitle("Current Weather • " + response.currently().summary() + " • " + response.currently().temperature() + "°F",
                        "https://merrysky.net/forecast/" + response.latitude() + "," + response.longitude())
                .setDescription(weather.toString())
                .setFooter(String.format("%s\n(%.5f,%.5f)", location.displayName(), response.latitude(), response.longitude()))
                .build());
        return Status.SUCCESS;
    }
}
