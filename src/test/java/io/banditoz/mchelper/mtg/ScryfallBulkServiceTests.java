package io.banditoz.mchelper.mtg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.BaseTest;
import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.database.dao.ScryfallDao;
import io.banditoz.mchelper.http.ScryfallBulkClient;
import io.banditoz.mchelper.http.ScryfallClient;
import io.jenetics.facilejdbc.Query;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@InjectTest
class ScryfallBulkServiceTests extends BaseTest {
    private static final String DOWNLOAD_URI = "https://data.scryfall.io/default-cards/default-cards.jsonl.gz";
    /** In the past, so once imported during a test, this bulk file counts as already imported. */
    private static final OffsetDateTime BULK_UPDATED_AT = OffsetDateTime.parse("2026-09-25T21:05:40.773Z");

    @Inject
    ObjectMapper objectMapper;
    @Inject
    ScryfallDao scryfallDao;
    @Inject
    Database db;

    private final ScryfallClient client = mock(ScryfallClient.class);
    private final ScryfallBulkClient bulkClient = mock(ScryfallBulkClient.class);
    private ScryfallBulkService service;
    private byte[] bulkFile;

    @BeforeEach
    void setup() throws IOException {
        truncate("mtg_cards");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream in = getClass().getResourceAsStream("/scryfall/bulk-sample.jsonl");
             GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            in.transferTo(gzip);
        }
        bulkFile = bytes.toByteArray();
        when(client.getDefaultCardBulkMetadata()).thenReturn(metadata(BULK_UPDATED_AT));
        when(bulkClient.download(any())).thenAnswer(invocation -> Response.builder()
                .status(200)
                .reason("OK")
                .request(Request.create(Request.HttpMethod.GET, DOWNLOAD_URI, Map.of(), null, StandardCharsets.UTF_8, null))
                .headers(Map.of())
                .body(bulkFile)
                .build());
        service = new ScryfallBulkService(client, bulkClient, objectMapper, scryfallDao, OpenTelemetry.noop().getTracer("test"));
    }

    @Test
    void loadBulkShouldImportEverythingButArtSeriesIntoEmptyTable() throws Exception {
        service.loadBulk();

        assertThat(countRows()).isEqualTo(5);
        assertThat(scryfallDao.lastImportedAt()).isPresent();
        verify(bulkClient).download(URI.create(DOWNLOAD_URI));
    }

    @Test
    void loadBulkShouldSkipWhenBulkDataIsAlreadyImported() throws Exception {
        service.loadBulk();
        service.loadBulk();

        verify(bulkClient, times(1)).download(any());
        assertThat(countRows()).isEqualTo(5);
    }

    @Test
    void loadBulkShouldReplaceRatherThanAppendWhenScryfallHasNewerData() throws Exception {
        when(client.getDefaultCardBulkMetadata())
                .thenReturn(metadata(BULK_UPDATED_AT), metadata(OffsetDateTime.now().plusDays(1)));

        service.loadBulk();
        service.loadBulk();

        verify(bulkClient, times(2)).download(any());
        assertThat(countRows()).isEqualTo(5);
    }

    @Test
    void lookupShouldPreferRegularPrintingOverNewerSecretLair() throws Exception {
        service.loadBulk();

        ScryfallCard card = scryfallDao.findByFuzzyName("Lightning Bolt").orElseThrow();
        assertThat(card.name()).isEqualTo("Lightning Bolt");
        assertThat(card.set()).isEqualTo("m10");
        assertThat(card.collectorNumber()).isEqualTo("146");
    }

    @Test
    void lookupShouldFindMultiFacedCardsByEitherFace() throws Exception {
        service.loadBulk();

        assertThat(scryfallDao.findByFuzzyName("insectile aberration").orElseThrow().name())
                .isEqualTo("Delver of Secrets // Insectile Aberration");
        assertThat(scryfallDao.findByFuzzyName("well").orElseThrow().name()).isEqualTo("Alive // Well");
        assertThat(scryfallDao.findByFuzzyName("lightnin bolt").orElseThrow().name()).isEqualTo("Lightning Bolt");
        assertThat(scryfallDao.findByFuzzyName("asdfqwer")).isEmpty();
    }

    private ScryfallBulkData metadata(OffsetDateTime updatedAt) {
        return new ScryfallBulkData(UUID.randomUUID().toString(), "default_cards", updatedAt,
                "https://api.scryfall.com/bulk-data/default_cards", DOWNLOAD_URI, bulkFile.length);
    }

    private long countRows() throws Exception {
        try (Connection c = db.getConnection()) {
            return Query.of("SELECT count(*) FROM mtg_cards").as((rs, conn) -> {
                rs.next();
                return rs.getLong(1);
            }, c);
        }
    }
}
