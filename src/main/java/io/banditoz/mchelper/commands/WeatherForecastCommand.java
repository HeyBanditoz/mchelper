package io.banditoz.mchelper.commands;

import com.google.common.collect.Iterables;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.http.DarkSkyClient;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.weather.IconGenerator;
import io.banditoz.mchelper.weather.darksky.Currently;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.darksky.DataItem;
import io.banditoz.mchelper.weather.geocoder.Location;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.banditoz.mchelper.weather.TemperatureConverter.fToCHU;
import static java.lang.Math.round;
import static net.dv8tion.jda.api.utils.messages.MessageCreateData.fromEditData;

@Requires(config = "mchelper.darksky.token")
public class WeatherForecastCommand extends Command {
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
        String locationToSearch = getLocationToSearch(ce);
        if (locationToSearch == null) return Status.FAIL;

        List<Location> locs = ce.getMCHelper().getNominatimLocationService().searchForLocation(locationToSearch);
        if (locs.isEmpty()) {
            ce.sendReply("Could not find location.");
            return Status.FAIL;
        }

        Location location = locs.get(0);
        DSWeather response = darkSkyClient.getForecast(location.lat(), location.lon());

        String threeHoursUuid = UUID.randomUUID().toString();
        String hourlyUuid = UUID.randomUUID().toString();
        String hourly48Uuid = UUID.randomUUID().toString();
        String dailyUuid = UUID.randomUUID().toString();

        Button threeHoursForecastButton = Button.primary(threeHoursUuid, "Three Hours");
        Button hourlyForecastButton = Button.primary(hourlyUuid, "Hourly (24h)");
        Button hourly48ForecastButton = Button.primary(hourly48Uuid, "Hourly (48h)");
        Button dailyForecastButton = Button.primary(dailyUuid, "Daily");
        Button stop = Button.danger(UUID.randomUUID().toString(), Emoji.fromUnicode("\uD83D\uDEAE"));

        Button threeHoursForecastButtonDisabled = Button.primary(threeHoursUuid, "Three Hours").asDisabled();
        Button hourlyForecastButtonDisabled = Button.primary(hourlyUuid, "Hourly (24h)").asDisabled();
        Button hourly48ForecastButtonDisabled = Button.primary(hourly48Uuid, "Hourly (48h)").asDisabled();
        Button dailyForecastButtonDisabled = Button.primary(dailyUuid, "Daily").asDisabled();

        MessageEditData threeHoursMessage = new MessageEditBuilder()
                .setActionRow(threeHoursForecastButtonDisabled, hourlyForecastButton, hourly48ForecastButton, dailyForecastButton, stop)
                .setEmbeds(getThreeHoursForecast(response, location))
                .build();
        MessageEditData hourlyMessage = new MessageEditBuilder()
                .setActionRow(threeHoursForecastButton, hourlyForecastButtonDisabled, hourly48ForecastButton, dailyForecastButton, stop)
                .setEmbeds(getHourlyForecast(response, location, false))
                .build();
        MessageEditData hourly48Message = new MessageEditBuilder()
                .setActionRow(threeHoursForecastButton, hourlyForecastButton, hourly48ForecastButtonDisabled, dailyForecastButton, stop)
                .setEmbeds(getHourlyForecast(response, location, true))
                .build();
        MessageEditData dailyMessage = new MessageEditBuilder()
                .setActionRow(threeHoursForecastButton, hourlyForecastButton, hourly48ForecastButton, dailyForecastButtonDisabled, stop)
                .setEmbeds(getDailyForecast(response, location))
                .build();

        ce.getEvent().getChannel().sendMessage(fromEditData(threeHoursMessage)).queue(message -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(
                            threeHoursForecastButton,
                            e -> e.getEvent().editMessage(threeHoursMessage).queue(),
                            hourlyForecastButton,
                            e -> e.getEvent().editMessage(hourlyMessage).queue(),
                            hourly48ForecastButton,
                            e -> e.getEvent().editMessage(hourly48Message).queue(),
                            dailyForecastButton,
                            e -> e.getEvent().editMessage(dailyMessage).queue(),
                            stop,
                            WrappedButtonClickEvent::removeListenerAndDestroy
                    ),
                    ce.getEvent().getAuthor()::equals, 60, message, ce);
            ce.getMCHelper().getButtonListener().addInteractable(i);
        });

        return Status.SUCCESS;
    }

    private MessageEmbed getThreeHoursForecast(DSWeather response, Location location) {
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
        return new EmbedBuilder()
                .setTitle("Current Weather Forecast • %s • %d°F / %s°C".formatted(c.summary(), round(c.temperature()), fToCHU(c.temperature())),
                        "https://merrysky.net/forecast/%s,%s".formatted(response.latitude(), response.longitude()))
                .setDescription(weather.toString())
                .setFooter(String.format("%s\n(%.5f,%.5f)", location.displayName(), response.latitude(), response.longitude()))
                .build();
    }

    private MessageEmbed getHourlyForecast(DSWeather response, Location location, boolean is48) {
        Currently c = response.currently();
        StringBuilder weather = new StringBuilder("When • Icon • Temp • Precip. Chance\n");
        // the size of the response.hourly().data() array should be 48, but in case it's less, guard against it here.
        for (int i = 0; i < (is48 ? Math.min(response.hourly().data().size(), 48) : Math.min(response.hourly().data().size(), 24)); i++) {
            DataItem data = response.hourly().data().get(i);
            weather.append(TimeFormat.DATE_TIME_SHORT.format(data.time()))
                    .append(" • ")
                    .append(IconGenerator.generateWeatherIcon(data.icon()))
                    .append(" • ")
                    .append("%d°F/%s°C".formatted(round(data.temperature()), fToCHU(data.temperature())))
                    .append(" • ")
                    .append("%d%%%s".formatted(round(data.precipProbability() * 100), data.getFormattedPrecipType()))
                    .append("\n");
        }
        return new EmbedBuilder()
                .setTitle("Current Weather Forecast • %s • %d°F / %s°C".formatted(c.summary(), round(c.temperature()), fToCHU(c.temperature())),
                        "https://merrysky.net/forecast/%s,%s".formatted(response.latitude(), response.longitude()))
                .setDescription(weather.toString())
                .setFooter(String.format("%s\n(%.5f,%.5f)", location.displayName(), response.latitude(), response.longitude()))
                .build();
    }

    private MessageEmbed getDailyForecast(DSWeather response, Location location) {
        Currently c = response.currently();
        StringBuilder weather = new StringBuilder("When • Icon • Temp LH F • Temp LH C • Precip. Chance\n");
        for (int i = 0; i < Math.min(response.daily().data().size(), 7); i++) {
            DataItem data = response.daily().data().get(i);
            weather.append(TimeFormat.DATE_LONG.format(data.time()))
                    .append(" • ")
                    .append(IconGenerator.generateWeatherIcon(data.icon()))
                    .append(" • ")
                    .append("%d°F/%s°F".formatted(round(data.temperatureLow()), round(data.temperatureHigh())))
                    .append(" • ")
                    .append("%s°C/%s°C".formatted(fToCHU(data.temperatureLow()), fToCHU(data.temperatureHigh())))
                    .append(" • ")
                    .append("%d%%%s".formatted(round(data.precipProbability() * 100), data.getFormattedPrecipType()))
                    .append("\n");
        }
        return new EmbedBuilder()
                .setTitle("Current Weather Forecast • %s • %d°F / %s°C".formatted(c.summary(), round(c.temperature()), fToCHU(c.temperature())),
                        "https://merrysky.net/forecast/%s,%s".formatted(response.latitude(), response.longitude()))
                .setDescription(weather.toString())
                .setFooter(String.format("%s\n(%.5f,%.5f)", location.displayName(), response.latitude(), response.longitude()))
                .build();
    }


    private String getLocationToSearch(CommandEvent ce) {
        String locationToSearch;
        if (ce.getCommandArgsString().isBlank()) {
            String configuredLocation = ce.getConfig().get(Config.WEATHER_DEFAULT_LOC);
            if (configuredLocation == null) {
                ce.sendReply("No location provided, and no Config.WEATHER_DEFAULT_LOC was specified.\n" +
                        "You can use this command to set a default: !config WEATHER_DEFAULT_LOC location here");
                return null;
            }
            else {
                locationToSearch = configuredLocation;
            }
        }
        else {
            locationToSearch = ce.getCommandArgsString();
        }
        return locationToSearch;
    }
}
