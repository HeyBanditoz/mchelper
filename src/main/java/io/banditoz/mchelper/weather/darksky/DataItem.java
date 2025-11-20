package io.banditoz.mchelper.weather.darksky;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;
import java.util.function.ToDoubleFunction;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DataItem(double windGust, int apparentTemperatureMinTime, double temperatureMax, String icon,
                       double precipIntensityMax, int windBearing, int temperatureMaxTime,
                       double apparentTemperatureMin, int sunsetTime, double temperatureLow, String precipType,
                       double humidity, double moonPhase, double windSpeed, int apparentTemperatureLowTime,
                       int sunriseTime, double apparentTemperatureLow, String summary, double precipProbability,
                       int temperatureHighTime, double visibility, double precipIntensity, double cloudCover,
                       double temperatureMin, int apparentTemperatureHighTime, double pressure, double dewPoint,
                       int temperatureMinTime, int uvIndexTime, double apparentTemperatureMax, double temperatureHigh,
                       int temperatureLowTime, double precipAccumulation, double apparentTemperatureHigh, Instant time,
                       int precipIntensityMaxTime, int windGustTime, double uvIndex, int apparentTemperatureMaxTime,
                       double precipIntensityError, double apparentTemperature, double ozone, double temperature)
        implements Precipitationable {

    // TODO expand with more methods to average *all* fields in DataItem? basic functionality here only, as we only care about what WeatherForecast uses, though, which is only averaging two fields
    // perhaps this would just be better as a classic mutable POJO...

    /**
     * Creates a new, single {@link DataItem}, averaging only the precipProbability and temperature fields.
     *
     * @param i A {@link List} of {@link DataItem DataItems} to reduce some fields for
     * @return A partially reduced, single {@link DataItem}.
     */
    public static DataItem reduceSome(List<DataItem> i) {
        DataItem f = i.get(0);
        // inefficient Stream abuse? abides the functional paradigm though, inefficiently though?
        return new DataItem(
                f.windGust, f.apparentTemperatureMinTime, f.temperatureMax, f.icon,
                f.precipIntensityMax, f.windBearing, f.temperatureMaxTime,
                f.apparentTemperatureMin, f.sunsetTime, f.temperatureLow, f.precipType,
                f.humidity, f.moonPhase, f.windSpeed, f.apparentTemperatureLowTime,
                f.sunriseTime, f.apparentTemperatureLow, f.summary, avg(DataItem::precipProbability, i),
                f.temperatureHighTime, f.visibility, f.precipIntensity, f.cloudCover,
                f.temperatureMin, f.apparentTemperatureHighTime, f.pressure, f.dewPoint,
                f.temperatureMinTime, f.uvIndexTime, f.apparentTemperatureMax, f.temperatureHigh,
                f.temperatureLowTime, f.precipAccumulation, f.apparentTemperatureHigh, f.time,
                f.precipIntensityMaxTime, f.windGustTime, f.uvIndex, f.apparentTemperatureMaxTime,
                f.precipIntensityError, f.apparentTemperature, f.ozone, avg(DataItem::temperature, i)
        );
    }

    private static <T> double avg(ToDoubleFunction<? super T> doubleGetter, List<T> potentialDoubles) {
        return potentialDoubles.stream().mapToDouble(doubleGetter).average().orElse(0);
    }
}