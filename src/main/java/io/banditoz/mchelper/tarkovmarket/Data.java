package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Data(@JsonProperty("items") List<Item> items) {
}
