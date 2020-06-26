package io.banditoz.mchelper.utils.weather.es;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.HttpResponseException;
import okhttp3.Request;
import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class GrafanaImageFetcher {
    private final MCHelper MCHELPER;

    public GrafanaImageFetcher(MCHelper mchelper) {
        this.MCHELPER = mchelper;
    }

    public ByteArrayInputStream fetchFahrenheit(int hourSince) throws IOException, HttpResponseException {
        hourSince = hourSince * -1;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hourSince);
        Date date = calendar.getTime();
        String url = MCHELPER.getSettings().getGrafanaUrl() + "render/d-solo/MQL_KW0Zz/weather?orgId=1&refresh=5s&from=" + date.toInstant().toEpochMilli() + "&to=" + Instant.now().toEpochMilli() + "&panelId=2&width=1500&height=750&tz=America%2FDenver"; // TODO holy shit
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + MCHELPER.getSettings().getGrafanaToken())
                .build();
        Response response = MCHELPER.performHttpRequestGetResponse(request);
        ByteArrayInputStream bytes = new ByteArrayInputStream(response.body().bytes());
        response.close();
        return bytes;
    }
}
