package io.banditoz.mchelper.utils;

import feign.Response;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.http.RedditLinkClient;

public class RedditLinkExtractor {
    private final RedditLinkClient client;

    public RedditLinkExtractor(MCHelper mcHelper) {
        this.client = mcHelper.getHttp().getRedditLinkClient();
    }

    public String extractFromRedditAppLink(String url) {
        try (Response response = client.extractRealLinkFromRedditAppLink(url.replaceAll("https://reddit.app.link/(.+)", "$1"))) {
            return response.headers()
                    .get("location")
                    .stream()
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Location header does not exist."))
                    .replaceAll("\\?utm_source.*", "");
        }
    }
}
