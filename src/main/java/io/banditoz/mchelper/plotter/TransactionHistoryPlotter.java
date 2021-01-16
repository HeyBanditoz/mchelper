package io.banditoz.mchelper.plotter;

import io.banditoz.mchelper.utils.database.Transaction;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class TransactionHistoryPlotter {
    private final String name;
    private final List<Transaction> transactions;

    public TransactionHistoryPlotter(String name, List<Transaction> transactions) {
        this.name = name;
        this.transactions = transactions;
    }

    /**
     * Plots this {@link TransactionHistoryPlotter} instance.
     *
     * @return A {@link ByteArrayOutputStream} containing the PNG image data.
     * @throws IOException If there was a problem generating the image.
     */
    public ByteArrayOutputStream plot() throws IOException {
        XYChart chart = new XYChartBuilder().title("Transaction history for " + name).width(1500).height(800).build();
        double[] y = transactions.stream().map(Transaction::getFinalAmount).mapToDouble(BigDecimal::doubleValue).toArray();
        chart.addSeries(" ", generate1ToN(transactions.size()), y);

        BufferedImage bi = BitmapEncoder.getBufferedImage(chart);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", baos);
        return baos;
    }

    private double[] generate1ToN(int n) {
        double[] array = new double[n];
        for (int i = 1; i < n; i++) {
            array[i - 1] = i;
        }
        return array;
    }
}
