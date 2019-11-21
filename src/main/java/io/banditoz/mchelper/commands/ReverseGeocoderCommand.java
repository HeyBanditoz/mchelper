package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.weather.GeoCoordinates;
import io.banditoz.mchelper.utils.weather.ReverseGeocoder;
import io.banditoz.mchelper.utils.weather.WeatherDeserializer;

public class ReverseGeocoderCommand extends Command {
    @Override
    public String commandName() {
        return "!rg";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<location>")
                .withDescription("Gets the geographic coordinates and a Google maps link given a location name.");
    }

    @Override
    public void onCommand() {
        ReverseGeocoder rg = new ReverseGeocoder(new WeatherDeserializer());
        GeoCoordinates g = null;
        try {
            g = rg.reverse(commandArgsString);
        } catch (Exception ex) {
            sendExceptionMessage(ex, true);
            return; //  we can't go any further, don't get an extra NPE
        }
        sendReply(g.toString());
    }
}
