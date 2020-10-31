package io.banditoz.mchelper.utils.paste;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PasteggUploader {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Logger LOGGER = LoggerFactory.getLogger(PasteggUploader.class);
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
        RequestBody body = RequestBody.create(JSON, MCHELPER.getObjectMapper().writeValueAsString(paste));
        Request request = new Request.Builder()
                .url("https://api.paste.gg/v1/pastes/")
                .post(body)
                .build();

        responseString = MCHELPER.performHttpRequest(request);
        LOGGER.debug("Response string: " + responseString);
        return buildUrl(MCHELPER.getObjectMapper().readValue(responseString, PasteResponse.class));
    }

    private String buildUrl(PasteResponse pr) {
        String id = pr.getResult().getId();
        String fileId = pr.getResult().getFiles().get(0).getId();

        return "https://paste.gg/p/anonymous/" + id + "/files/" + fileId + "/raw";
    }
}
