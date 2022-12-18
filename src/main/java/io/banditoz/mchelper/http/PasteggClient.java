package io.banditoz.mchelper.http;

import feign.Headers;
import feign.RequestLine;
import io.banditoz.mchelper.utils.paste.Paste;
import io.banditoz.mchelper.utils.paste.PasteResponse;

@Headers({"Accept: application/json", "Content-Type: application/json"})
public interface PasteggClient {
    @RequestLine("POST /v1/pastes")
    PasteResponse uploadPaste(Paste paste);
}
