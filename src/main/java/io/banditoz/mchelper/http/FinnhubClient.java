package io.banditoz.mchelper.http;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.banditoz.mchelper.investing.model.CompanyProfile;
import io.banditoz.mchelper.investing.model.Quote;
import io.banditoz.mchelper.investing.model.RawCandlestick;

@Headers({"Accept: application/json"})
public interface FinnhubClient {
    @RequestLine("GET /v1/stock/profile2?symbol={symbol}")
    CompanyProfile getCompanyProfile(@Param("symbol") String symbol);

    @RequestLine("GET /v1/quote?symbol={symbol}")
    Quote getQuote(@Param("symbol") String symbol);

    @RequestLine("GET /v1/quote?symbol={symbol}&resolution={resolution}&from={from}&to={to}")
    RawCandlestick getCandles(
            @Param("symbol") String symbol,
            @Param("resolution")String resolution,
            @Param("from") String from,
            @Param("to") String to
    );
}
