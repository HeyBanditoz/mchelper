package io.banditoz.mchelper.utils.paste;

import io.banditoz.mchelper.MCHelper;

public class PasteggUploader {
    private final MCHelper MCHELPER;

    public PasteggUploader(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    /**
     * Uploads a message to paste.gg.
     *
     * @return The URL of the paste.
     */
    public String uploadToPastegg(Paste paste) {
        PasteResponse pResponse = MCHELPER.getHttp().getPasteggClient().uploadPaste(paste);
        return buildUrl(pResponse);
    }

    private String buildUrl(PasteResponse pr) {
        String baseUrl = MCHELPER.getSettings().getPasteGgBaseUrl();
        baseUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
        return baseUrl + pr.getResult().getId();
    }
}
