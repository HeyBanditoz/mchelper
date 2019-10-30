package io.banditoz.mchelper.utils.dictionary;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import io.banditoz.mchelper.utils.SettingsManager;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class DictionarySearcher {
    public static DictionaryResult search(String word) throws IOException, HttpResponseException {
        Request request = new Request.Builder()
                .url("https://owlbot.info/api/v3/dictionary/" + word)
                .addHeader("Authorization", "Token " + SettingsManager.getInstance().getSettings().getOwlBotToken())
                .build();
        Response response = MCHelper.getOkHttpClient().newCall(request).execute();
        if (response.code() >= 400) {
            throw new HttpResponseException(response.code());
        }
        return MCHelper.getObjectMapper().readValue(response.body().string(), DictionaryResult.class);
    }
}
