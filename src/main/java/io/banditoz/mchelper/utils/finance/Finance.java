package io.banditoz.mchelper.utils.finance;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.DateUtils;
import io.banditoz.mchelper.utils.HttpResponseException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;

import javax.imageio.ImageIO;
import java.awt.Color;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Finance {
    private final MCHelper MCHELPER;
    private final String API_KEY;
    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public Finance(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
        this.API_KEY = mcHelper.getSettings().getAlphaVantageKey();
    }

    public RealtimeCurrencyExchangeRate getCurrencyExchangeRate(String from, String to) throws IOException, HttpResponseException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.alphavantage.co")
                .addPathSegment("query")
                .addQueryParameter("function", "CURRENCY_EXCHANGE_RATE")
                .addQueryParameter("from_currency", from)
                .addQueryParameter("to_currency", to)
                .addQueryParameter("apikey", API_KEY)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return MCHELPER.getObjectMapper().readValue(MCHELPER.performHttpRequest(request), Currency.class).getRealtimeCurrencyExchangeRate();
    }

    private OHLCDataset getStock(String ticker, String interval) throws IOException, HttpResponseException, ParseException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.alphavantage.co")
                .addPathSegment("query")
                .addQueryParameter("function", "TIME_SERIES_INTRADAY")
                .addQueryParameter("symbol", ticker)
                .addQueryParameter("interval", interval)
                .addQueryParameter("outputsize", "full")
                .addQueryParameter("datatype", "csv")
                .addQueryParameter("apikey", API_KEY)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        String resp = MCHELPER.performHttpRequest(request);
        return generateOHLCDatasetFromTicker(resp, ticker);
    }

    public GlobalQuote getGlobalQuote(String ticker) throws IOException, HttpResponseException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.alphavantage.co")
                .addPathSegment("query")
                .addQueryParameter("function", "GLOBAL_QUOTE")
                .addQueryParameter("symbol", ticker)
                .addQueryParameter("datatype", "json")
                .addQueryParameter("apikey", API_KEY)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return MCHELPER.getObjectMapper().readValue(MCHELPER.performHttpRequest(request), GlobalQuote.class).getGlobalQuote();
    }

    public BestMatchesItem getTickerBestMatch(String ticker) throws IOException, HttpResponseException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.alphavantage.co")
                .addPathSegment("query")
                .addQueryParameter("function", "SYMBOL_SEARCH")
                .addQueryParameter("keywords", ticker)
                .addQueryParameter("apikey", API_KEY)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        List<BestMatchesItem> bestMatches = MCHELPER.getObjectMapper().readValue(MCHELPER.performHttpRequest(request), TickerMatches.class).getBestMatches();
        bestMatches.sort(Comparator.comparing(BestMatchesItem::getMatchScore)); // can never be too sure
        return bestMatches.get(bestMatches.size() - 1); // get last in list, which is best score.
    }

    public MessageEmbed generateMessagEmbedFromGlobalQuote(GlobalQuote gq, String name) {
        Color c;
        if (gq.getChangePercent().signum() == 1) {
            c = Color.GREEN;
        }
        else {
            c = Color.RED;
        }
        return new EmbedBuilder()
                .setTitle(name)
                .addField("Price", gq.getPrice().toString(), true)
                .addField("Open", gq.getOpen().toString(), true)
                .addField("Close", gq.getPreviousClose().toString(), true)
                .addField("High", gq.getHigh().toString(), true)
                .addField("Low", gq.getLow().toString(), true)
                .addField("Change", gq.getChange().toString(), true)
                .addField("Change Percent", gq.getChangePercent().toString() + "%", true)
                .setColor(c)
                .build();
    }

    private OHLCDataset generateOHLCDatasetFromTicker(String s, String ticker) throws ParseException {
        s = s.substring(s.indexOf('\n') + 1); // get rid of the top line
        ArrayList<OHLCDataItem> items = new ArrayList<>();
        Date today = new Date();

        for (String line : s.split("\n")) {
            StringTokenizer st = new StringTokenizer(line, ",");
            Date date = DF.parse(st.nextToken());
            double open = Double.parseDouble(st.nextToken());
            double high = Double.parseDouble(st.nextToken());
            double low = Double.parseDouble(st.nextToken());
            double close = Double.parseDouble(st.nextToken());
            double volume = Double.parseDouble(st.nextToken());
            items.add(new OHLCDataItem(date, open, high, low, close, volume));
        }
        items.removeIf(item -> !DateUtils.isSameDay(item.getDate(), today)); // TODO Figure out how to get last trading day
        OHLCDataItem[] itemsArray = new OHLCDataItem[items.size()];
        for (int i = 0; i < items.size(); i++) {
            itemsArray[i] = items.get(i);
        }
        return new DefaultOHLCDataset(ticker, itemsArray);
    }

    public ByteArrayOutputStream generateStockGraph(String ticker, String interval, String realName) throws IOException, HttpResponseException, ParseException {
        OHLCDataset dataset = getStock(ticker, interval);
        JFreeChart chart = ChartFactory.createCandlestickChart(realName, "Time", "Price", dataset, true);
        XYPlot plot = (XYPlot) chart.getPlot();

        plot.getChart().removeLegend();
        plot.setBackgroundPaint(Color.WHITE); // light yellow = new Color(0xffffe0)
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false);
        BufferedImage bi = chart.createBufferedImage(1000, 500);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", byteArray);
        return byteArray;
    }
}
