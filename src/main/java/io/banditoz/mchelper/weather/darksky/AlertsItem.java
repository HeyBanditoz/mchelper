package io.banditoz.mchelper.weather.darksky;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlertsItem(String severity, int expires, List<String> regions, String description, int time, String title,
						 String uri) {
}