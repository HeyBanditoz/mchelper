package io.banditoz.mchelper.http;

import java.net.URI;

import feign.Headers;
import feign.RequestLine;
import feign.Response;

@Headers({"Accept: */*"})
public interface ScryfallBulkClient {
    // does this even deserve its own client if it just accepts a URI lol
    @RequestLine("GET")
    Response download(URI downloadUri);
}
