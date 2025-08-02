package io.banditoz.mchelper.investing;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

import io.banditoz.mchelper.database.dao.CompanyProfileDao;
import io.banditoz.mchelper.http.FinnhubClient;
import io.banditoz.mchelper.investing.model.CompanyProfile;
import io.banditoz.mchelper.investing.model.Quote;
import io.banditoz.mchelper.investing.model.RawCandlestick;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;

@Singleton
public class Finance {
    private final CompanyProfileDao companyProfileDao;
    private final FinnhubClient client;

    @Inject
    public Finance(@Nullable CompanyProfileDao companyProfileDao,
                   FinnhubClient client) {
        this.companyProfileDao = companyProfileDao;
        this.client = client;
    }

    public Quote getQuote(String ticker) {
        return client.getQuote(ticker.toUpperCase());
    }

    public CompanyProfile getCompanyProfile(String ticker) {
        // first, check SQL cache
        ticker = ticker.toUpperCase();
        Optional<CompanyProfile> companyProfile = companyProfileDao == null ? Optional.empty() : companyProfileDao.getCompanyProfile(ticker);
        // nothing, get from API
        if (companyProfile.isEmpty()) {
            companyProfile = Optional.of(client.getCompanyProfile(ticker));
            if (companyProfileDao != null) {
                companyProfileDao.addCompanyProfileIfNotExists(companyProfile.get());
            }
        }
        return companyProfile.get();
    }

    public RawCandlestick getCandlestickForTicker(String ticker, boolean yearly) {
        ticker = ticker.toUpperCase();
        Calendar open = Calendar.getInstance();
        if (!yearly) {
            open.set(Calendar.HOUR_OF_DAY, 6);
            open.set(Calendar.MINUTE, 0);
            open.set(Calendar.SECOND, 0);
            open.setTimeZone(TimeZone.getTimeZone("MST"));
            if (open.get(Calendar.HOUR_OF_DAY) <= 7) { // okay, let's NOT CHECK FOR HOUR AND MINUTE OF DAY! FUCK YOU!
                if (open.get(Calendar.MINUTE) > 30) {
                    open.set(Calendar.DAY_OF_WEEK, open.get(Calendar.DAY_OF_WEEK) - 1);
                }
            }
            if (open.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || open.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                open.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); // this is god-fucking awful. I hate this so much. Gets last trading day (maybe) though. Does not account for holidays, too bad!
            }
        }
        return client.getCandles(
                ticker,
                yearly ? "D" : String.valueOf(5),
                String.valueOf(yearly ? (Instant.now().minus(364, ChronoUnit.DAYS).getEpochSecond()) : open.getTimeInMillis() / 1000),
                String.valueOf(System.currentTimeMillis() / 1000)
        );
    }

    public static MessageEmbed generateStockMessageEmbed(Quote quote, CompanyProfile cp) {
        Color c = quote.getChangePercent() > 0 ? Color.GREEN : Color.RED;
        return new EmbedBuilder()
                .setTitle(cp.getName() == null ? "<unknown ticker>" : cp.getName())
                .setDescription(cp.getFinnhubIndustry() == null ? "<unknown industry>" : cp.getFinnhubIndustry())
                .addField("Price", String.valueOf(quote.getCurrentPrice()), true)
                .addField("Change", String.valueOf(quote.getChange()), true)
                .addField("Change Percent", String.valueOf(quote.getChangePercent()) + '%', true)
                .addField("High", String.valueOf(quote.getHigh()), true)
                .addField("Low", String.valueOf(quote.getLow()), true)
                .setColor(c)
                .build();
    }

    public static ByteArrayOutputStream generateStockGraph(RawCandlestick candles, CompanyProfile cp) throws IOException {
        String name = cp.getName() == null ? "null" : cp.getName();
        OHLCChart chart = new OHLCChartBuilder().width(2000).height(750).title(name).build();
        chart.addSeries("Series", candles.getAsDates(), candles.getOpens(), candles.getHighs(), candles.getLows(), candles.getCloses(), candles.getVolumes())
                .setUpColor(Color.GREEN)
                .setDownColor(Color.RED);
        chart.getStyler().setLegendVisible(false);
        BufferedImage bi = BitmapEncoder.getBufferedImage(chart);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", baos);
        return baos;
    }
}
