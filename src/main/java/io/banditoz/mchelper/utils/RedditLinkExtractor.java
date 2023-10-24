package io.banditoz.mchelper.utils;

import io.banditoz.mchelper.Http;
import io.banditoz.mchelper.MCHelper;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RedditLinkExtractor {
    private final Http http;

    public RedditLinkExtractor(MCHelper mcHelper) {
        this.http = mcHelper.getHttp();
    }

    public String extractFromRedditAppLink(String url) throws IOException {
        Request request = getBaseRequest().url(url).build();
        try (Response response = http.placeNonRedirectingRequest(request)) {
            return response.header("Location").replaceAll("\\?utm_source.*", "");
        }
    }

    public String extractFromRedditShareLink(String url) throws IOException {
        try (Response response = http.placeNonRedirectingRequest(getBaseRequest().url(url).build())) {
            // need to redirect *just once more*
            String loc = response.header("Location");
            try (okhttp3.Response response2 = http.placeNonRedirectingRequest(getBaseRequest().url(loc).build())) {
                return response2.header("Location").split("\\?")[0]; // strip tracking shit
            }
        }
    }

    private Request.Builder getBaseRequest() {
        return new Request.Builder() // we fake user agent or else we won't get redirect to real reddit
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux; I HATE YOUR WEBISTE) Well well well... It seems as though you have once again, despite your best efforts, become a pawn in one of my rather elaborate ruses, and it seems as though the end result of said ruse has left your state of being of a lesser quality than before you were dealt the card hidden up my sleeve, while being none the wiser! And yes, while it is true that you will eventually recover from this recent turn of events, it remains unclear whether or not your social status on this website will remain at its current level, or if it will take a turn for the worst! After all is said and done, at the end of the day, you will have to accept the fact that you just got the short end of the deal! I hold no remorse or regret, for I am and always shall be........ someone who believes any link coming from your damned website should redirect to reddit, not amp reddit, not mobile reddit, just good old reddit");
    }
}
