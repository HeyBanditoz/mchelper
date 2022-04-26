package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public record TraderPrice(@JsonProperty("priceRUB") int price,
                          @JsonProperty("trader") Trader trader) implements Comparable<TraderPrice> {
    @Override
    public int compareTo(@NotNull TraderPrice o) {
        return Integer.compare(o.price, price);
    }
}
