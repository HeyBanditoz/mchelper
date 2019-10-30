package io.banditoz.mchelper.utils.dictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.SettingsManager;
import okhttp3.Request;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DictionarySearcher {
    public static DictionaryResult search(String word) throws IOException, HttpResponseException {
        Request request = new Request.Builder()
                .url("https://owlbot.info/api/v3/dictionary/" + word)
                .addHeader("Authorization", "Token " + SettingsManager.getInstance().getSettings().getOwlBotToken())
                .build();
        String json = MCHelper.performHttpRequest(request);
        LoggerFactory.getLogger(DictionarySearcher.class).debug(json);
        return MCHelper.getObjectMapper().readValue(json, DictionaryResult.class);
    }
}
