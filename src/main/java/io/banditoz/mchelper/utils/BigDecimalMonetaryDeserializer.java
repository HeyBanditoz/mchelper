package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalMonetaryDeserializer extends NumberDeserializers.BigDecimalDeserializer {
    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return super.deserialize(p, ctx).setScale(2, RoundingMode.HALF_UP); // round up from two decimals
    }
}
