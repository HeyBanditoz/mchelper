package io.banditoz.mchelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.*;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.banditoz.mchelper.http.*;
import io.banditoz.mchelper.utils.Settings;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static feign.FeignException.errorStatus;

public class Http {
    private final OkHttpClient client;
    private final TarkovClient tarkovClient;
    private final PasteggClient pasteggClient;
    private final UrbanDictionaryClient urbanDictionaryClient;
    private final RedditLinkClient redditLinkClient;
    private final FinnhubClient finnhubClient;
    private final OwlbotClient owlbotClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(Http.class);

    public Http(MCHelper mcHelper) {
        LOGGER.info("Building Feign HTTP clients...");
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        feign.okhttp.OkHttpClient feignClient = new feign.okhttp.OkHttpClient(client);
        Settings s = mcHelper.getSettings();
        ObjectMapper om = mcHelper.getObjectMapper();
        JacksonDecoder d = new JacksonDecoder(om);
        JacksonEncoder e = new JacksonEncoder(om);
        UserAgentInterceptor uai = new UserAgentInterceptor();
        BodyRedactingErrorDecoder er = new BodyRedactingErrorDecoder();

        tarkovClient = (s.getTarkovToolsApiEndpoint() != null) ? Feign.builder()
                .client(feignClient)
                .decoder(d)
                .errorDecoder(er)
                .requestInterceptor(uai)
                .target(TarkovClient.class, s.getTarkovToolsApiEndpoint()) : null;

        if (s.getPasteGgApiEndpoint() != null) {
            Feign.Builder b = Feign.builder()
                    .client(feignClient)
                    .decoder(d)
                    .encoder(e)
                    .errorDecoder(er)
                    .requestInterceptors(List.of())
                    .requestInterceptor(uai);
            if (s.getPasteGgApiKey() != null) {
                b.requestInterceptor(template -> template.header("Authorization", "Key " + s.getPasteGgApiKey()));
            }
            pasteggClient = b.target(PasteggClient.class, s.getPasteGgApiEndpoint());
        }
        else {
            pasteggClient = null;
        }

        urbanDictionaryClient = Feign.builder()
                .client(feignClient)
                .decoder(d)
                .errorDecoder(er)
                .requestInterceptor(uai)
                .target(UrbanDictionaryClient.class, "https://api.urbandictionary.com");

        // this client doesn't follow redirects to fool the stupid reddit link fetching
        redditLinkClient = Feign.builder()
                .client(feignClient)
                .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, false))
                .target(RedditLinkClient.class, "https://reddit.app.link");

        finnhubClient = mcHelper.getSettings().getFinnhubKey() != null ? Feign.builder()
                    .client(feignClient)
                    .decoder(d)
                    .errorDecoder(er)
                    .requestInterceptor(uai)
                    .requestInterceptor(template -> template.header("X-Finnhub-Token", mcHelper.getSettings().getFinnhubKey()))
                    .target(FinnhubClient.class, "https://finnhub.io/api") : null;

        owlbotClient = mcHelper.getSettings().getOwlBotToken() != null ? Feign.builder()
                .client(feignClient)
                .decoder(d)
                .errorDecoder(er)
                .requestInterceptor(uai)
                .requestInterceptor(template -> template.header("Authorization", "Token " + mcHelper.getSettings().getOwlBotToken()))
                .target(OwlbotClient.class, "https://owlbot.info/api") : null;

        LOGGER.info("Finished building Feign clients. Current status: " + this);
    }

    public TarkovClient getTarkovClient() {
        return tarkovClient;
    }

    public PasteggClient getPasteggClient() {
        return pasteggClient;
    }

    public UrbanDictionaryClient getUrbanDictionaryClient() {
        return urbanDictionaryClient;
    }

    public RedditLinkClient getRedditLinkClient() {
        return redditLinkClient;
    }

    public FinnhubClient getFinnhubClient() {
        return finnhubClient;
    }

    public OwlbotClient getOwlbotCLient() {
        return owlbotClient;
    }

    private static final class UserAgentInterceptor implements RequestInterceptor {
        private static final String javaVersion = System.getProperty("java.version");

        @Override
        public void apply(RequestTemplate template) {
            template.header("User-Agent", "MCHelper/" + Version.GIT_SHA + " Java/" + javaVersion + " okhttp/" + OkHttp.VERSION + " Feign (+https://gitlab.com/HeyBanditoz/mchelper)");
        }
    }

    private static final class BodyRedactingErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            return errorStatus(methodKey, response, 0, 0);
        }
    }

    @Override
    public String toString() {
        return "Http{" +
                "client=" + client +
                ", tarkovClient=" + tarkovClient +
                ", pasteggClient=" + pasteggClient +
                ", urbanDictionaryClient=" + urbanDictionaryClient +
                ", redditLinkClient=" + redditLinkClient +
                ", finnhubClient=" + finnhubClient +
                ", owlbotClient=" + owlbotClient +
                '}';
    }
}
