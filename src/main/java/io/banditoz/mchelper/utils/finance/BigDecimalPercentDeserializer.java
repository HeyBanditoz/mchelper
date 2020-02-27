package io.banditoz.mchelper.utils.finance;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalPercentDeserializer extends NumberDeserializers.BigDecimalDeserializer {
    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String newValue = p.getText().replace("%", ""); // TODO this whole thing is potentially unsafe, we aren't using jackson's deserialization after removing the % from the string!
        return new BigDecimal(newValue).setScale(2, RoundingMode.HALF_UP); // round up from two decimals
    }
}
