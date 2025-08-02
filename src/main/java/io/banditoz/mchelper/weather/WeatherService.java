package io.banditoz.mchelper.weather;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import io.avaje.config.Config;
import io.avaje.inject.RequiresProperty;
import io.banditoz.mchelper.UserEvent;
import io.banditoz.mchelper.http.DarkSkyClient;
import io.banditoz.mchelper.llm.LLMService;
import io.banditoz.mchelper.llm.anthropic.AnthropicRequest;
import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.geocoder.Location;
import io.banditoz.mchelper.weather.geocoder.NominatimLocationService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresProperty("mchelper.darksky.token")
public class WeatherService {
    private final DarkSkyClient darkSkyClient;
    private final NominatimLocationService nominatimLocationService;
    private final LLMService llmService;
    private final boolean llmWeatherSummariesEnabled;
    private static final DateTimeFormatter LLM_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm:ss z");
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Inject
    public WeatherService(DarkSkyClient darkSkyClient,
                          NominatimLocationService nominatimLocationService,
                          @Nullable LLMService llmService) {
        this.darkSkyClient = darkSkyClient;
        this.nominatimLocationService = nominatimLocationService;
        this.llmService = llmService;
        this.llmWeatherSummariesEnabled = Config.getBool("mchelper.darksky.llm-summaries-enabled", true);
    }

    public ForecastSummary getSummaryForLocation(String locationToSearch, UserEvent ue) {
        List<Location> locs = nominatimLocationService.searchForLocation(locationToSearch);
        if (locs.isEmpty()) {
            throw new NoSuchElementException("Location not found.");
        }
        Location location = locs.get(0);
        DSWeather forecast = darkSkyClient.getForecast(location);
        return new ForecastSummary(forecast, location, llmService == null ? null : getSummaryForForecast(forecast, ue));
    }

    public ForecastSummary getForecastForLocation(String locationToSearch) {
        List<Location> locs = nominatimLocationService.searchForLocation(locationToSearch);
        if (locs.isEmpty()) {
            throw new NoSuchElementException("Location not found.");
        }
        Location location = locs.get(0);
        DSWeather forecast = darkSkyClient.getForecast(location);
        return new ForecastSummary(forecast, location, null);
    }

    public String getSummaryForForecast(DSWeather forecast, UserEvent ue) {
        if (llmService == null || !llmWeatherSummariesEnabled) {
            return null;
        }
        AnthropicRequest request = new AnthropicRequest.PromptBuilder()
                /*
                 * summary of available Anthropic models, as of 2024-07-27:
                 * claude-3-haiku-20240307 fast, cheap, and sorta inaccurate and wild with responses (doesn't seem to follow instructions very well?)
                 * claude-3-opus-20240229 EXPENSIVE and SLOW!! ~8 sec API time
                 * claude-3-5-sonnet-20240620 is a nice middle ground between the two
                 */
                .setModel("claude-3-5-sonnet-20240620")
                .setSystem("You are an expert, concise weather summarizer for weather forecasts.")
                .setMaxTokens(1024)
                .setInitialMessage(buildAiPrompt(forecast))
                .setTemperature(0.5)
                .build();

        String response;
        try {
            response = llmService.getSingleResponse(request, ue);
        } catch (Exception ex) {
            response = "Exception generating weather summary from LLM. Check the logs.";
            log.error("Exception generating weather summary from the LLM.", ex);
        }

        return response;
    }

    private String buildAiPrompt(DSWeather forecast) {
        String dailyForecastCsv = getDailyForecastAsCsv(forecast);
        String hourlyForecastCsv = getHourlyForecastAsCsv(forecast);

        return """
               Analyze the provided daily weather forecast and give a concise, one-to-two sentence summary on
               either major changes in weather, if there will be any, or the weather in general.
               
               <constraints>
                 Do not specify at the start this is a weather forecast. The user will already know this, given the context.
                 Do not specify "forecast period" but instead give dates and times.
                 Instead of saying "Fahrenheit" say "°F"
                 Do not attempt to forecast future dates. Only forecast based on the forecast provided. The data that is
                   provided *is* a forecast.
               </constraints>
               
               The data will be given to you in the CSV format, where each line is a CSV column.
               Each date will cover the range of that entire date. For reference. the current date and time is %s
               The daily format is as follows, where each column is separated by newline:
               <format>
                 The date and time of the forecast at the top of the hour, in format 07-26 (as July 26th)
                 The highest temperature forecasted for the day, in Fahrenheit.
                 The lowest temperature forecasted for the day, in Fahrenheit.
                 The change of precipitation (not necessarily rain, see below.) in range 0.0 to 1.0.
                 The type of precipitation.
                 The humidity, in range 0.0 to 1.0.
                 The cloud cover, in range 0.0 to 1.0.
                 The wind speed, in miles per hour.
                 The wind gust, in miles per hour.
                 The wind bearing, as 0-360.
               </format>
               
               The daily forecast in CSV format is as follows:
               <data>
               %s
               </data>
               
               The hourly forecast format is as follows:
               <format>
                 The date and time of the forecast at the top of the hour.
                 The average temperature forecasted for this hour, in Fahrenheit.
                 The change of precipitation (not necessarily rain, see below.) in range 0.0 to 1.0.
                 The type of precipitation.
                 The humidity, in range 0.0 to 1.0.
                 The cloud cover, in range 0.0 to 1.0.
                 The wind speed, in miles per hour.
                 The wind gust, in miles per hour.
                 The wind bearing, as 0-360.
               </format>
               
               The hourly forecast in CSV format is as follows:
               <data>
               %s
               </data>
               """.formatted(DT_FORMAT.format(Instant.now().atZone(ZoneId.systemDefault())), dailyForecastCsv, hourlyForecastCsv);
    }

    private String getHourlyForecastAsCsv(DSWeather forecast) {
        return forecast.hourly().data()
                .stream()
                .map(i -> "%s,%s,%s,%s,%s,%s,%s,%s,%s"
                        .formatted(
                                DT_FORMAT.format(i.time().atZone(ZoneId.systemDefault())),
                                i.temperature(),
                                i.precipProbability(),
                                i.precipType(),
                                i.humidity(),
                                i.cloudCover(),
                                i.windSpeed(),
                                i.windGust(),
                                i.windBearing()
                        ))
                .collect(Collectors.joining("\n"));
    }

    private String getDailyForecastAsCsv(DSWeather forecast) {
        return forecast.daily().data()
                .stream()
                .map(i -> "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s"
                        .formatted(
                                LLM_DATE_FORMAT.format(i.time().atZone(ZoneId.systemDefault())),
                                i.temperatureHigh(),
                                i.temperatureLow(),
                                i.precipProbability(),
                                i.precipType(),
                                i.humidity(),
                                i.cloudCover(),
                                i.windSpeed(),
                                i.windGust(),
                                i.windBearing()
                        ))
                .collect(Collectors.joining("\n"));
    }
}
