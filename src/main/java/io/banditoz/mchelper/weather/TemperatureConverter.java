package io.banditoz.mchelper.weather;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class TemperatureConverter {
    private static final DecimalFormat DF;

    static {
        DF = new DecimalFormat("0");
        DF.setRoundingMode(RoundingMode.HALF_UP);
    }

    /**
     * Converts Fahrenheit to Celsius.
     *
     * @param f Fahrenheit value.
     * @return Celsius value.
     */
    public static double fToC(double f) {
        return (f - 32.0) / 1.8;
    }

    /**
     * Converts Fahrenheit to Celsius, rounding up from .5 onwards to nearest whole number, else down.
     *
     * @param f Fahrenheit value.
     * @return Rounded Celsius value, as a String.
     */
    public static String fToCHU(double f) {
        return DF.format(fToC(f));
    }
}
