package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Trader(@JsonProperty("name") TraderName trader) {
}

enum TraderName {
    @JsonProperty("Prapor")
    PRAPOR,
    @JsonProperty("Therapist")
    THERAPIST,
    @JsonProperty("Fence")
    FENCE,
    @JsonProperty("Skier")
    SKIER,
    @JsonProperty("Peacekeeper")
    PEACEKEEPER,
    @JsonProperty("Mechanic")
    MECHANIC,
    @JsonProperty("Ragman")
    RAGMAN,
    @JsonProperty("Jaeger")
    JAEGER;
}
