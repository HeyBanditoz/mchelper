package io.banditoz.mchelper.weather.darksky;

import java.util.List;

public record Hourly(String summary, List<DataItem> data, String icon) {
}