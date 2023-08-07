package io.banditoz.mchelper.weather.darksky;

import java.util.List;

public record Daily(String summary, List<DataItem> data, String icon) {
}