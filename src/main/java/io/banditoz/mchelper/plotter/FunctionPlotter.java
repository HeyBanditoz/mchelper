package io.banditoz.mchelper.plotter;

import com.udojava.evalex.Expression;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class FunctionPlotter {
    private final Expression EXPRESSION;
    private final ArrayList<Double> X_DATA;
    private final ArrayList<Double> Y_DATA;
    private final double X_MIN;
    private final double X_MAX;
    private final double Y_MIN;
    private final double Y_MAX;

    /**
     * Constructs a new FunctionPlotter class, pre-generating the data necessary to generate a graph.
     *
     * @param math The math to plot
     * @param xMin smallest x value
     * @param xMax largest x value
     * @param yMin smallest y value
     * @param yMax largest y value
     * @param step quality of the graph
     */
    public FunctionPlotter(String math, double xMin, double xMax, double yMin, double yMax, double step) {
        // sanity checks
        if (xMin >= xMax) {
            throw new IllegalArgumentException("bad range");
        }
        if (yMin >= yMax) {
            throw new IllegalArgumentException("bad domain");
        }
        int size = (int) Math.round((Math.abs(xMin) + Math.abs(xMax)) / step);
        if (size <= step) {
            throw new IllegalArgumentException("bad step");
        }
        this.X_MIN = xMin;
        this.X_MAX = xMax;
        this.Y_MIN = yMin;
        this.Y_MAX = yMax;
        EXPRESSION  = new Expression(math);
        X_DATA = new ArrayList<>(size);
        Y_DATA = new ArrayList<>(size);
        for (double x = X_MIN; x < X_MAX; x += step) {
            EXPRESSION.setVariable("x", BigDecimal.valueOf(x));
            EXPRESSION.setVariable("x", BigDecimal.valueOf(x));
            try {
                X_DATA.add(x);
                Y_DATA.add(EXPRESSION.eval().doubleValue());
            } catch (Expression.ExpressionException | ArithmeticException ex) {
                // there is a good chance that the number does not exist at the given point, add null
                Y_DATA.add(null);
            }
        }
    }

    /**
     * Plots this {@link FunctionPlotter} instance.
     *
     * @return A {@link ByteArrayOutputStream containing the PNG image data.}
     * @throws IOException If there was a problem generating the image.
     */
    public ByteArrayOutputStream plot() throws IOException {
        XYChart chart = new XYChartBuilder().title(EXPRESSION.getExpression()).width(600).height(609).build();
        XYSeries series = chart.addSeries(" ", X_DATA, Y_DATA);
        series.setMarker(SeriesMarkers.NONE);
        series.setSmooth(true);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisMin(X_MIN);
        chart.getStyler().setXAxisMax(X_MAX);
        chart.getStyler().setYAxisMin(Y_MIN);
        chart.getStyler().setYAxisMax(Y_MAX);
        BufferedImage bi = BitmapEncoder.getBufferedImage(chart);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", baos);
        return baos;
    }

    public Expression getExpression() {
        return EXPRESSION;
    }
}
