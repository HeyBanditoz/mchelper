package io.banditoz.mchelper.weather.darksky;

import java.util.List;

public record AlertsItem(String severity, int expires, List<String> regions, String description, int time, String title,
						 String uri) {
}