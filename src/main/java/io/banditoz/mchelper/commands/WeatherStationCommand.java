package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.weather.es.EsUtils;
import io.banditoz.mchelper.utils.weather.es.GrafanaImageFetcher;

import java.io.IOException;

public class WeatherStationCommand extends Command {
    @Override
    public String commandName() {
        return "!ws";
    }

    @Override
    public void onCommand() {
        int hourSince = 24;
        if (commandArgs.length > 1) {
            hourSince = Integer.parseInt(commandArgsString);
        }
        try {
            e.getChannel().sendMessage(EsUtils.getLatestFormattedWeather())
                    .addFile(GrafanaImageFetcher.fetchFahrenheit(hourSince), "graph.png")
                    .queue();
        } catch (IOException ex) {
            sendExceptionMessage(ex, true);
        }
    }
}
