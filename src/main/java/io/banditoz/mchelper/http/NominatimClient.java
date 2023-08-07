package io.banditoz.mchelper.http;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.banditoz.mchelper.weather.geocoder.Location;

import java.util.List;

@Headers({"Accept: application/json"})
public interface NominatimClient {
    @RequestLine("GET /search?format=json&q={loc}")
    List<Location> searchForLocation(@Param("loc") String loc);
}
