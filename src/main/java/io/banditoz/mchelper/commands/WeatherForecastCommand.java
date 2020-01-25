package io.banditoz.mchelper.commands;

import ch.rasc.darksky.DsClient;
import ch.rasc.darksky.model.*;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.weather.GeoCoordinates;
import io.banditoz.mchelper.utils.weather.IconGenerator;
import io.banditoz.mchelper.utils.weather.ReverseGeocoder;
import io.banditoz.mchelper.utils.weather.WeatherDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    protected void onCommand(CommandEvent ce) {
        ReverseGeocoder g = new ReverseGeocoder(new WeatherDeserializer());
        GeoCoordinates c;
        try {
            c = g.reverse(ce.getCommandArgsString());
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex, true);
            return;
        }
        DsClient client = new DsClient(SettingsManager.getInstance().getSettings().getDarkSkyAPI(), new WeatherDeserializer(), MCHelper.getOkHttpClient());
        DsForecastRequest request = DsForecastRequest.builder()
                .latitude(String.valueOf(c.getLatitude()))
                .longitude(String.valueOf(c.getLongitude()))
                .excludeBlock(DsBlock.ALERTS, DsBlock.MINUTELY, DsBlock.DAILY)
                .unit(DsUnit.US)
                .build();
        DsResponse response = null;
        try {
            response = client.sendForecastRequest(request);
        } catch (IOException ex) {
            ce.sendExceptionMessage(ex, true);
        }
        StringBuilder weather = new StringBuilder("Current Weather • " + response.currently().summary() + "\nDate-Time • Icon • Temp • Percip. Chance\n");
        for (DsDataPoint data : response.hourly().data()) {
            weather.append(formatter.format(Instant.ofEpochSecond(data.time())))
                    .append(" • ")
                    .append(IconGenerator.generateWeatherIcon(data.icon()))
                    .append(" • ")
                    .append(data.temperature())
                    .append("°F • ")
                    .append(data.precipProbability().multiply(new BigDecimal("100")))
                    .append("%\n");
        }
        weather.append(g.getDisplayName()).append("\n").append("Powered by Dark Sky");
        ce.sendReply(weather.toString());
    }
}
