package io.banditoz.mchelper.tarkovmarket;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Vendor(@JsonProperty("name") VendorName vendorName) {
}
