package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Trader(@JsonProperty("name") String name) {
}