package io.banditoz.mchelper.utils;

import static java.lang.Math.*;

public class ProgressBar {
    private static final String[] blocks = new String[]{"", "▏", "▎", "▍", "▌", "▋", "▊", "▉", "█"};
    private static final double base = 0.125;

    /**
     * Renders a unicode block-based progress bar.
     *
     * @param value  Current value to be displayed as progress.
     * @param vmin   Minimum value, usually 0.0.
     * @param vmax   Maximum value, usually 1.0.
     * @param length Length of the progress bar, in characters.
     * @return A rendered progress bar.
     * @author <a href="https://gist.github.com/rougier/c0d31f5cbdaac27b876c">rougier</a>
     */
    public static String generateProgressBar(double value, double vmin, double vmax, int length) {
        // normalize value
        value = min(max(value, vmin), vmax);
        value = (value - vmin) / (vmax - vmin);

        double v = value * length;
        int x = (int) floor(v); // integer part
        double y = v - x;       // fractional part
        // round(base*math.floor(float(y)/base),prec)/base
        int i = (int) ((round((base * floor(y / base)) * 1000D) / base) / 1000D);
        String bar = "█".repeat(x) + blocks[i];
        int n = length - bar.length();
        return bar + " ".repeat(n);
    }
}
