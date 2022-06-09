package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public record VendorPrice(@JsonProperty("priceRUB") int price,
                          @JsonProperty("vendor") Vendor vendor) implements Comparable<VendorPrice> {
    @Override
    public int compareTo(@NotNull VendorPrice o) {
        return Integer.compare(o.price, price);
    }
}
