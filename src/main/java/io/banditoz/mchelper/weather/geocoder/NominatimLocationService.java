package io.banditoz.mchelper.weather.geocoder;

import io.banditoz.mchelper.http.NominatimClient;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.List;

public class NominatimLocationService {
    private final NominatimClient client;
    private final Cache<String, List<Location>> locationCache;

    public NominatimLocationService(NominatimClient client) {
        this.client = client;
        this.locationCache = new Cache2kBuilder<String, List<Location>>() {}
                .eternal(true)
                .suppressExceptions(false)
                .build();
    }

    /**
     * Searches for a location using OpenStreetMap's <a href="https://nominatim.org/">Nominatim</a> service, which is an
     * alternative to Google's <a href="https://developers.google.com/maps/documentation/geocoding/overview">Geocoding API.</a>
     *
     * @param location The plain text location to search for.
     * @return The location. Potentially multiple of OSM's Nominatim service returns as such.
     */
    public List<Location> searchForLocation(String location) {
        return locationCache.computeIfAbsent(location, () -> client.searchForLocation(location));
    }
}
