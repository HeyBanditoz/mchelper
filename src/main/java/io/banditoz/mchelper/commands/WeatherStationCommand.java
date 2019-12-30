package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.weather.es.EsUtils;
import io.banditoz.mchelper.utils.weather.es.GrafanaImageFetcher;

public class WeatherStationCommand extends Command {
    @Override
    public String commandName() {
        return "ws";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[hours]")
                .withDescription("Returns various statistics of my weather station, including a Fahrenheit graph.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        int hourSince = 24;
        if (ce.getCommandArgs().length > 1) {
            hourSince = Integer.parseInt(ce.getCommandArgsString());
        }
        try {
            ce.getEvent().getChannel().sendMessage(EsUtils.getLatestFormattedWeather() + "\nGraph shows weather from the past " + hourSince + " hour(s).")
                    .addFile(GrafanaImageFetcher.fetchFahrenheit(hourSince), "graph.png")
                    .queue();
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex, true);
        }
    }
}
