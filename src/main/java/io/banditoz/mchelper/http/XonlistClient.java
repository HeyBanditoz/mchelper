package io.banditoz.mchelper.http;

import feign.Headers;
import feign.RequestLine;
import io.banditoz.mchelper.xonlist.XonlistResponse;

@Headers({"Accept: application/json"})
public interface XonlistClient {
    @RequestLine("GET /endpoint/json")
    XonlistResponse getAllXonoticServers();
}
