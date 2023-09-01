package io.banditoz.mchelper.motd;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import io.banditoz.mchelper.weather.darksky.Currently;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.darksky.DataItem;
import io.banditoz.mchelper.weather.geocoder.Location;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;

import static io.banditoz.mchelper.weather.TemperatureConverter.fToCHU;
import static java.lang.Math.round;

public class WeatherMotdSectionGenerator extends MotdSectionGenerator {
    private static final Color EMBED_COLOR = new Color(212, 19, 196);
    public WeatherMotdSectionGenerator(MCHelper mcHelper) {
        super(mcHelper);
    }

    @Override
    public MessageEmbed generate(TextChannel tc) {
        long guildId = tc.getGuild().getIdLong();
        ConfigurationProvider c = mcHelper.getConfigurationProvider();
        String val = c.getValue(Config.WEATHER_DEFAULT_LOC, guildId);
        if (val == null) {
            return new EmbedBuilder()
                    .setTitle("No location configured!")
                    .setDescription("Have a guild admin set one with " + c.getValue(Config.PREFIX, guildId) + "config WEATHER_DEFAULT_LOC location here!")
                    .setColor(EMBED_COLOR)
                    .build();
        }
        Location location = mcHelper.getNominatimLocationService()
                .searchForLocation(c.getValue(Config.WEATHER_DEFAULT_LOC, guildId))
                .get(0);
        DSWeather response = mcHelper.getHttp().getDarkSkyClient().getCurrentlyDaily(location);

        // TODO move to proper WeatherService later in the future to centralize forecast embed generation
        Currently currently = response.currently();
        DataItem today = response.daily().data().get(0);
        return new EmbedBuilder()
                .setTitle("Current Weather Forecast • %s • %d°F / %s°C".formatted(currently.summary(), round(currently.temperature()), fToCHU(currently.temperature())),
                        "https://merrysky.net/forecast/%s,%s".formatted(response.latitude(), response.longitude()))
                .addField("Low/High", "%s°F/%s°F\n%s°C/%s°C"
                        .formatted(round(today.temperatureMin()), round(today.temperatureMax()), fToCHU(today.temperatureMin()), fToCHU(today.temperatureMax())), true)
                .addField("Humidity", "%s%%".formatted(round(today.humidity() * 100D)), true)
                .addField("Wind", "%s mph".formatted(round(today.windSpeed())), true)
                .addField("Precipitation", "%s%%".formatted(round(today.precipProbability() * 100D)), true)
                .addField("Pressure", "%s mb\n%.3f atm".formatted(round(today.pressure()), today.pressure() / 1013.0), true)
                .setFooter(String.format("%s\n(%.5f,%.5f)", location.displayName(), response.latitude(), response.longitude()))
                .setColor(EMBED_COLOR)
                .build();
    }
}
