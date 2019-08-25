package io.banditoz.mchelper.utils;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface Ticker {
    double getPrice();
    double get24h();
    void makeAPICall(String uri, List<NameValuePair> parameters) throws URISyntaxException, IOException;
}
