package io.banditoz.mchelper.utils.weather.es;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.SettingsManager;
import okhttp3.Request;
import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class GrafanaImageFetcher {
    public static ByteArrayInputStream fetchFahrenheit(int hourSince) throws IOException {
        hourSince = hourSince * -1;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hourSince);
        Date date = calendar.getTime();
        String url = SettingsManager.getInstance().getSettings().getGrafanaUrl() + "render/d-solo/MQL_KW0Zz/weather?orgId=1&refresh=5s&from=" + date.toInstant().toEpochMilli() + "&to=" + Instant.now().toEpochMilli() + "&panelId=2&width=1500&height=750&tz=America%2FDenver"; // TODO holy shit
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SettingsManager.getInstance().getSettings().getGrafanaToken())
                .build();
        Response response = MCHelper.getOkHttpClient().newCall(request).execute();
        return new ByteArrayInputStream(response.body().bytes());
    }
}
