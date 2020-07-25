package io.banditoz.mchelper.investing;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.investing.model.CompanyProfile;
import io.banditoz.mchelper.investing.model.Quote;
import io.banditoz.mchelper.investing.model.RawCandlestick;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.database.dao.CompanyProfileDao;
import io.banditoz.mchelper.utils.database.dao.CompanyProfileDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.knowm.xchart.*;

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

public class Finance {
    private final MCHelper MCHELPER;
    private final String API_KEY;

    public Finance(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
        this.API_KEY = mcHelper.getSettings().getFinnhubKey();
    }

    public Quote getQuote(String ticker) throws IOException, HttpResponseException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("finnhub.io")
                .addPathSegment("api")
                .addPathSegment("v1")
                .addPathSegment("quote")
                .addQueryParameter("symbol", ticker)
                .addQueryParameter("token", API_KEY)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return MCHELPER.getObjectMapper().readValue(MCHELPER.performHttpRequest(request), Quote.class);
    }

    public CompanyProfile getCompanyProfile(String ticker) throws IOException, HttpResponseException {
        // first, check SQL cache
        CompanyProfileDao dao = new CompanyProfileDaoImpl(MCHELPER.getDatabase());
        Optional<CompanyProfile> companyProfile = dao.getCompanyProfile(ticker);
        // nothing, get from API
        if (companyProfile.isEmpty()) {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("finnhub.io")
                    .addPathSegment("api")
                    .addPathSegment("v1")
                    .addPathSegment("stock")
                    .addPathSegment("profile2")
                    .addQueryParameter("symbol", ticker)
                    .addQueryParameter("token", API_KEY)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            companyProfile = Optional.of(MCHELPER.getObjectMapper().readValue(MCHELPER.performHttpRequest(request), CompanyProfile.class));
            dao.addCompanyProfileIfNotExists(companyProfile.get());
        }
        return companyProfile.get();
    }

    public RawCandlestick getCandlestickForTicker(String ticker, boolean yearly) throws IOException, HttpResponseException {
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
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("finnhub.io")
                .addPathSegment("api")
                .addPathSegment("v1")
                .addPathSegment("stock")
                .addPathSegment("candle")
                .addQueryParameter("symbol", ticker)
                .addQueryParameter("resolution", yearly ? "D" : String.valueOf(5))
                .addQueryParameter("from", String.valueOf(yearly ? (Instant.now().minus(364, ChronoUnit.DAYS).getEpochSecond()) : open.getTimeInMillis() / 1000))
                .addQueryParameter("to", String.valueOf(System.currentTimeMillis() / 1000))
                .addQueryParameter("token", API_KEY)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return MCHELPER.getObjectMapper().readValue(MCHELPER.performHttpRequest(request), RawCandlestick.class);
    }

    public static MessageEmbed generateStockMessageEmbed(Quote quote, CompanyProfile cp) {
        Color c;
        if (quote.getChangePercent() > 0) {
            c = Color.GREEN;
        }
        else {
            c = Color.RED;
        }
        return new EmbedBuilder()
                .setTitle(cp.getName())
                .setDescription(cp.getFinnhubIndustry())
                .addField("Price", String.valueOf(quote.getCurrentPrice()), true)
                .addField("Change", String.valueOf(quote.getChange()), true)
                .addField("Change Percent", String.valueOf(quote.getChangePercent()) + '%', true)
                .addField("High", String.valueOf(quote.getHigh()), true)
                .addField("Low", String.valueOf(quote.getLow()), true)
                .setColor(c)
                .build();
    }

    public static ByteArrayOutputStream generateStockGraph(RawCandlestick candles, CompanyProfile cp) throws IOException {
        OHLCChart chart = new OHLCChartBuilder().width(2000).height(750).title(cp.getName()).build();
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
