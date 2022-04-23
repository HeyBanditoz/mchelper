package io.banditoz.mchelper.utils.paste;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class PasteggUploader {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final MCHelper MCHELPER;

    public PasteggUploader(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    /**
     * Uploads a message to paste.gg.
     *
     * @return The URL of the paste.
     */
    public String uploadToPastegg(Paste paste) throws IOException, HttpResponseException {
        String responseString;
        RequestBody body = RequestBody.create(MCHELPER.getObjectMapper().writeValueAsString(paste), JSON);
        Request.Builder builder = new Request.Builder()
                .url(MCHELPER.getSettings().getPasteGgApiEndpoint() + "/v1/pastes/")
                .post(body);
        if (MCHELPER.getSettings().getPasteGgApiKey() != null) {
            builder.addHeader("Authorization", "Key " + MCHELPER.getSettings().getPasteGgApiKey());
        }
        responseString = MCHELPER.performHttpRequest(builder.build());
        return buildUrl(MCHELPER.getObjectMapper().readValue(responseString, PasteResponse.class));
    }

    private String buildUrl(PasteResponse pr) {
        String baseUrl = MCHELPER.getSettings().getPasteGgBaseUrl();
        baseUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
        return baseUrl + pr.getResult().getId();
    }
}
