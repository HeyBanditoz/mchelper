package io.banditoz.mchelper.weather.darksky;

import java.util.List;

public record Minutely(String summary, List<DataItem> data, String icon) {
}