package io.banditoz.mchelper.weather.darksky;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Minutely(String summary, List<DataItem> data, String icon) {
}