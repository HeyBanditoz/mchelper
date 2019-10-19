package io.banditoz.mchelper.commands;

import ch.rasc.darksky.DsClient;
import ch.rasc.darksky.model.DsBlock;
import ch.rasc.darksky.model.DsForecastRequest;
import ch.rasc.darksky.model.DsResponse;
import ch.rasc.darksky.model.DsUnit;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.GeoCoordinates;
import io.banditoz.mchelper.utils.ReverseGeocoder;
import io.banditoz.mchelper.utils.SettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;

public class WeatherCommand extends Command {
    @Override
    public String commandName() {
        return "!w";
    }

    @Override
    public void onCommand() {
        ReverseGeocoder g = new ReverseGeocoder(MCHelper.client);
        GeoCoordinates c = null;
        try {
            c = g.reverse(commandArgsString);
        } catch (IOException ex) {
            sendExceptionMessage(ex, true);
        }
        DsClient client = new DsClient(SettingsManager.getInstance().getSettings().getDarkSkyAPI(), MCHelper.client);
        DsForecastRequest request = DsForecastRequest.builder()
                .latitude(String.valueOf(c.getLatitude()))
                .longitude(String.valueOf(c.getLongitude()))
                .excludeBlock(DsBlock.ALERTS, DsBlock.MINUTELY, DsBlock.HOURLY)
                .unit(DsUnit.US)
                .build();
        DsResponse response = null;
        try {
            response = client.sendForecastRequest(request);
        } catch (IOException ex) {
            sendExceptionMessage(ex, true);
        }
        EmbedBuilder b = new EmbedBuilder()
                .setTitle("Current Weather • " + response.currently().summary(),
                        "https://darksky.net/forecast/" + response.latitude().toString() + "," + response.longitude().toString())
                .setDescription(response.daily().summary())
                .addField("Temperature", response.currently().temperature().toString() +
                        "°F (feels like " + response.currently().apparentTemperature().toString() + "°F)" ,true)
                .addField("Humidity", response.currently().humidity().doubleValue() * 100 + "%", true)
                .addField("Wind", response.currently().windSpeed().toString() + " mph", true)
                .addField("Precipitation", response.currently().precipProbability().doubleValue() * 100 + "%", true)
                .addField("Pressure", response.currently().pressure().toString() + " mb", true)
                .setFooter(g.getDisplayName() + " • Powered by Dark Sky");
        sendEmbedReply(b.build());
    }
}
