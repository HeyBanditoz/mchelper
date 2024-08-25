package io.banditoz.mchelper.http;

import feign.Headers;
import feign.RequestLine;
import io.banditoz.mchelper.tarkovmarket.Response;

import java.util.Map;

@Headers({"Accept: application/json", "Content-Type: application/json"})
public interface TarkovClient {
    @RequestLine("POST")
    Response getTarkovMarketData(Map<String, String> body);
}