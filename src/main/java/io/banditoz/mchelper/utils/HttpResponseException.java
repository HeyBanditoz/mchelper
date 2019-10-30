package io.banditoz.mchelper.utils;

public class HttpResponseException extends Exception {
    public HttpResponseException(int code) {
        super("Response was not successful! Code " + code);
    }
}
