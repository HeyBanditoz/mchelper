package io.banditoz.mchelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static feign.FeignException.errorStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import feign.*;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.banditoz.mchelper.http.*;
import io.banditoz.mchelper.utils.Whitebox;
import io.banditoz.mchelper.weather.geocoder.Location;
import jakarta.inject.Inject;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class Http {
    private final OkHttpClient client;
    private final OkHttpClient nonRedirectingClient;
    private final feign.Client feignClient;
    private final TarkovClient tarkovClient;
    private final PasteggClient pasteggClient;
    private final UrbanDictionaryClient urbanDictionaryClient;
    private final FinnhubClient finnhubClient;
    private final OwlbotClient owlbotClient;
    private final NominatimClient nominatimClient;
    private final DarkSkyClient darkSkyClient;
    private final AnthropicClient anthropicClient;
    private final ScryfallClient scryfallClient;

    private final JacksonDecoder decoder;
    private final JacksonEncoder encoder;
    private final UserAgentInterceptor userAgentInterceptor;
    private final BodyRedactingErrorDecoder bodyRedactingErrorDecoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(Http.class);

    @Inject
    public Http(ObjectMapper om) {
        LOGGER.info("Building Feign HTTP clients...");
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS);
        // what the hell? why is this necessary? thanks kotlin...
        builder.setFollowRedirects$okhttp(false);
        builder.setFollowSslRedirects$okhttp(false);
        nonRedirectingClient = builder.build();

        this.feignClient = new feign.okhttp.OkHttpClient(client);
        this.decoder = new JacksonDecoder(om);
        this.encoder = new JacksonEncoder(om);
        this.userAgentInterceptor = new UserAgentInterceptor();
        this.bodyRedactingErrorDecoder = new BodyRedactingErrorDecoder();

        tarkovClient = Config.getOptional("mchelper.endpoints.tarkov-market")
                .map(url -> baseFeignBuilder()
                        .encoder(encoder)
                        .retryer(new Retryer.Default(1000L, 3000L, 2))
                        .target(TarkovClient.class, url))
                .orElse(null);

        pasteggClient = Config.getOptional("mchelper.pastegg.api-url")
                .map(url -> {
                    Feign.Builder b = baseFeignBuilder()
                            .encoder(encoder);
                    String key = Config.getNullable("mchelper.pastegg.api-key");
                    if (key != null) {
                        b.requestInterceptor(template -> template.header("Authorization", "Key " + key));
                    }
                    return b.target(PasteggClient.class, url);
                })
                .orElse(null);

        darkSkyClient = Config.getOptional("mchelper.darksky.token")
                .map(token -> baseFeignBuilder()
                        .requestInterceptor(template -> template.uri(template.url().replace("APIKEY", token)))
                        .responseInterceptor((r, c) -> {
                            try {
                                int remain = Integer.parseInt(r.response().headers().get("X-RateLimit-Remaining-Month").iterator().next());
                                int limit = Integer.parseInt(r.response().headers().get("X-RateLimit-Limit-Month").iterator().next());
                                if (remain % 10 == 0) {
                                    LOGGER.info("DarkSky-compat X-RateLimit-Remaining-Month={} X-RateLimit-Limit-Month={}", remain, limit);
                                }
                            }
                            catch (NoSuchElementException ex) {
                                LOGGER.warn("Could not extract ratelimit info from " + r.response() + ", headers not present?", ex);
                            }
                            catch (Exception ex) {
                                LOGGER.warn("Generic error decoding headers for ratelimit info.", ex);
                            }
                            return r.proceed();
                        })
                        .retryer(new Retryer.Default(1000L, 7500L, 7))
                        .target(PirateWeatherClient.class, "https://api.pirateweather.net"))
                .orElse(null);

        urbanDictionaryClient = baseFeignBuilder()
                .target(UrbanDictionaryClient.class, "https://api.urbandictionary.com");

        finnhubClient = Config.getOptional("mchelper.finnhub.token")
                .map(token -> baseFeignBuilder()
                        .requestInterceptor(template -> template.header("X-Finnhub-Token", token))
                        .target(FinnhubClient.class, "https://finnhub.io/api"))
                .orElse(null);

        owlbotClient = Config.getOptional("mchelper.owlbot.token")
                .map(token -> baseFeignBuilder()
                        .requestInterceptor(template -> template.header("Authorization", "Token " + token))
                        .target(OwlbotClient.class, "https://owlbot.info/api"))
                .orElse(null);

        nominatimClient = baseFeignBuilder()
                .decoder(new JsonArrayJacksonDecoder(om, Location.class))
                .target(NominatimClient.class, "https://nominatim.openstreetmap.org");

        anthropicClient = Config.getOptional("mchelper.anthropic.token")
                .map(token -> baseFeignBuilder()
                        .encoder(encoder)
                        .errorDecoder(bodyRedactingErrorDecoder)
                        .options(new Request.Options(30, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true))
                        .requestInterceptor(template -> template.header("x-api-key", token))
                        .target(AnthropicClient.class, Config.get("mchelper.anthropic.endpoint", "https://api.anthropic.com")))
                .orElse(null);

        scryfallClient = baseFeignBuilder()
                .errorDecoder(bodyRedactingErrorDecoder)
                .target(ScryfallClient.class, "https://api.scryfall.com");

        LOGGER.info("Finished building Feign clients. Current status: " + this);
    }

    private Feign.Builder baseFeignBuilder() {
        return new Feign.Builder()
                .client(feignClient)
                .decoder(decoder)
                .options(new Request.Options(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true))
                .requestInterceptor(userAgentInterceptor);
    }

    @Bean
    public OkHttpClient getClient() {
        return client;
    }

    @Bean
    public Optional<TarkovClient> getTarkovClient() {
        return Optional.ofNullable(tarkovClient);
    }

    @Bean
    public Optional<PasteggClient> getPasteggClient() {
        return Optional.ofNullable(pasteggClient);
    }

    @Bean
    public UrbanDictionaryClient getUrbanDictionaryClient() {
        return urbanDictionaryClient;
    }

    @Bean
    public Optional<FinnhubClient> getFinnhubClient() {
        return Optional.ofNullable(finnhubClient);
    }

    @Bean
    public Optional<OwlbotClient> getOwlbotCLient() {
        return Optional.ofNullable(owlbotClient);
    }

    /**
     * @apiNote Don't use this. Go through the caching
     * {@link io.banditoz.mchelper.weather.geocoder.NominatimLocationService#searchForLocation(String)} instead.
     */
    @Bean
    public Optional<NominatimClient> getNominatimClient() {
        return Optional.ofNullable(nominatimClient);
    }

    @Bean
    public Optional<DarkSkyClient> getDarkSkyClient() {
        return Optional.ofNullable(darkSkyClient);
    }

    @Bean
    public Optional<AnthropicClient> getAnthropicClient() {
        return Optional.ofNullable(anthropicClient);
    }

    @Bean
    public ScryfallClient getScryfallClient() {
        return scryfallClient;
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

    // TODO just write a custom exception handler instead of this hacky reflection shit lol, but this is quick and lazy :)
    private static final class BodyUrlRedactingErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            Whitebox.setInternalState(response.request(), "url", "REDACTED");
            return errorStatus(methodKey, response, 0, 0);
        }
    }

    /**
     * Copied from feign-jackson-12.3 to support lists.
     *
     * @param mapper The {@link ObjectMapper} to use.
     * @param type   The type that we should deserialize to in the {@link List}.
     */
    private record JsonArrayJacksonDecoder(ObjectMapper mapper, Class<?> type) implements Decoder {
        @Override
        public List<?> decode(Response response, Type type) throws IOException {
            if (response.status() == 404 || response.status() == 204)
                return Collections.emptyList();
            if (response.body() == null)
                return null;
            Reader reader = response.body().asReader(response.charset());
            if (!reader.markSupported()) {
                reader = new BufferedReader(reader, 1);
            }
            try {
                // Read the first byte to see if we have any data
                reader.mark(1);
                if (reader.read() == -1) {
                    return null; // Eagerly returning null avoids "No content to map due to end-of-input"
                }
                reader.reset();
                return mapper.readValue(reader, mapper.getTypeFactory().constructCollectionType(List.class, this.type));
            } catch (RuntimeJsonMappingException e) {
                if (e.getCause() != null && e.getCause() instanceof IOException) {
                    throw IOException.class.cast(e.getCause());
                }
                throw e;
            }
        }
    }

    public okhttp3.Response placeNonRedirectingRequest(okhttp3.Request request) throws IOException {
        try (okhttp3.Response response = nonRedirectingClient.newCall(request).execute()) {
            return response;
        }
    }

    @Override
    public String toString() {
        return "Http{" +
                "client=" + client +
                ", nonRedirectingClient=" + nonRedirectingClient +
                ", tarkovClient=" + tarkovClient +
                ", pasteggClient=" + pasteggClient +
                ", urbanDictionaryClient=" + urbanDictionaryClient +
                ", finnhubClient=" + finnhubClient +
                ", owlbotClient=" + owlbotClient +
                ", nominatimClient=" + nominatimClient +
                ", darkSkyClient=" + darkSkyClient +
                ", anthropicClient=" + anthropicClient +
                ", scryfallClient=" + scryfallClient +
                '}';
    }
}
