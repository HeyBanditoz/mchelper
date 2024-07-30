package io.banditoz.mchelper.weather;

import io.banditoz.mchelper.weather.darksky.DSWeather;
import io.banditoz.mchelper.weather.geocoder.Location;

import javax.annotation.Nullable;

/**
 * @param forecast The forecast.
 * @param location The location.
 * @param llmSummary The LLM summary of the forecast. Will be null if an LLM
 *                   provider is not configured.
 */
public record ForecastSummary(DSWeather forecast, Location location, @Nullable String llmSummary) {
}
