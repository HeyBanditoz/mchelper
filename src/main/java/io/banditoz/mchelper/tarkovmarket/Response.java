package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Response(@JsonProperty("data") Data data) {
}