package io.banditoz.mchelper.utils;

import io.banditoz.mchelper.MCHelper;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.HttpResponseException;

import java.io.IOException;

public class ExtractRedditLink {
    public static String extractFromRedditAppLink(String url) throws IOException {
        Request request = new Request.Builder() // we fake user agent or else we won't get redirect to real reddit
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:71.0) Gecko/20100101 Firefox/71.0")
                .url(url)
                .build();
        Response response = MCHelper.client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new HttpResponseException(response.code(), "Response was not successful!");
        }
        return response.header("Location").replaceAll("\\?utm_source.*", "");
    }
}
