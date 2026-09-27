package io.banditoz.mchelper.mtg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;
import feign.Response;
import io.banditoz.mchelper.database.ScryfallCardRow;
import io.banditoz.mchelper.database.dao.ScryfallDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.http.ScryfallBulkClient;
import io.banditoz.mchelper.http.ScryfallClient;
import io.banditoz.mchelper.telemetry.Tracing;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresDatabase // bulk cards get persisted, otherwise there's no point in creating this singleton
public class ScryfallBulkService {
    private static final Logger log = LoggerFactory.getLogger(ScryfallBulkService.class);
    /** Set types whose printings are what you'd open in a pack or deck, as opposed to box sets, promos, tokens, etc. */
    private static final Set<String> NORMAL_SET_TYPES = Set.of("core", "expansion", "masters", "draft_innovation",
            "commander", "starter", "duel_deck");
    private static final Set<String> REPRINT_LIST_SETS = Set.of("plst", "mb2");
    private static final Set<String> NORMAL_BORDERS = Set.of("black", "white");
    private static final Set<String> SPECIAL_FRAME_EFFECTS = Set.of("showcase", "extendedart", "inverted", "etched",
            "borderless", "fullart", "textless");
    private static final LocalDate MODERN_FRAME_START = LocalDate.of(2003, 7, 1);

    private final ScryfallClient client;
    private final ScryfallBulkClient bulkClient;
    private final ObjectMapper objectMapper;
    private final ScryfallDao scryfallDao;
    private final Tracer tracer;
    private final ReentrantLock importLock = new ReentrantLock();

    @Inject
    public ScryfallBulkService(ScryfallClient client, ScryfallBulkClient bulkClient, ObjectMapper objectMapper,
                               ScryfallDao scryfallDao, Tracer tracer) {
        this.client = client;
        this.bulkClient = bulkClient;
        this.objectMapper = objectMapper;
        this.scryfallDao = scryfallDao;
        this.tracer = tracer;
    }

    public void loadBulk() throws IOException, SQLException {
        if (!importLock.tryLock()) {
            throw new IllegalStateException("A Scryfall bulk import is already running.");
        }
        Span span = tracer.spanBuilder("scryfall bulk import")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        try (Scope ignored = span.makeCurrent()) {
            importBulk(span, false);
        } catch (IOException | SQLException | RuntimeException e) {
            Tracing.recordException(span, e);
            throw e;
        } finally {
            span.end();
            importLock.unlock();
        }
    }

    private boolean importBulk(Span span, boolean force) throws IOException, SQLException {
        long before = System.currentTimeMillis();
        ScryfallBulkData metadata = client.getDefaultCardBulkMetadata();
        span.setAttribute("scryfall.bulk.updated_at", metadata.updatedAt().toString());
        span.setAttribute("scryfall.bulk.compressed_size", metadata.compressedSize());
        if (!force) {
            Optional<OffsetDateTime> lastImportedAt = scryfallDao.lastImportedAt();
            if (lastImportedAt.isPresent() && !metadata.updatedAt().isAfter(lastImportedAt.get())) {
                span.setAttribute("scryfall.skipped", true);
                log.info("Skipping Scryfall bulk import, their bulk data from {} was already imported at {}.",
                        metadata.updatedAt(), lastImportedAt.get());
                return false;
            }
        }
        log.info("Update needed or no cards found, starting Scryfall bulk import!");
        ObjectReader objectReader = objectMapper.readerFor(ObjectNode.class);

        int processed;
        try (Response bulkData = bulkClient.download(URI.create(metadata.jsonlDownloadUri()))) {
            if (bulkData.status() != 200) {
                throw new IOException("Scryfall bulk download failed with HTTP " + bulkData.status());
            }
            try (InputStream inputStream = new GZIPInputStream(bulkData.body().asInputStream());
                 MappingIterator<ObjectNode> it = objectReader.readValues(inputStream)) {
                Iterator<ScryfallCardRow> rows = Streams.stream(it)
                        .filter(card -> !"art_series".equals(card.path("layout").asText()))
                        .map(this::toRow)
                        .iterator();
                processed = scryfallDao.replaceAll(rows);
            }
        }
        span.setAttribute("scryfall.cards_imported", processed);
        log.info("Processed {} cards in {} ms.", processed, (System.currentTimeMillis() - before));
        return true;
    }

    private ScryfallCardRow toRow(ObjectNode card) {
        String name = card.get("name").asText();
        LocalDate releasedAt = LocalDate.parse(card.get("released_at").asText());
        // reversible_card printings have no top-level oracle_id, only per face, so fall back to the first face's
        JsonNode oracleId = card.has("oracle_id") ? card.get("oracle_id") : card.path("card_faces").path(0).get("oracle_id");
        return new ScryfallCardRow(
                UUID.fromString(card.get("id").asText()),
                UUID.fromString(oracleId.asText()),
                name,
                name.toLowerCase(Locale.ROOT),
                card.get("lang").asText(),
                card.get("set").asText(),
                card.get("collector_number").asText(),
                releasedAt,
                card.get("layout").asText(),
                isNormalPrint(card, releasedAt),
                card.hasNonNull("edhrec_rank") ? card.get("edhrec_rank").asInt() : null,
                card.toString()
        );
    }

    /**
     * Whether a printing is a regular one, the kind a name lookup should show before any promo, showcase frame,
     * Secret Lair, token, reprint list, and so on.
     */
    static boolean isNormalPrint(JsonNode card, LocalDate releasedAt) {
        String frame = card.path("frame").asText();
        JsonNode games = card.path("games");
        return NORMAL_SET_TYPES.contains(card.path("set_type").asText())
                && !REPRINT_LIST_SETS.contains(card.path("set").asText())
                && !card.path("promo").asBoolean()
                && !card.path("variation").asBoolean()
                && !card.path("full_art").asBoolean()
                && !card.path("oversized").asBoolean()
                && NORMAL_BORDERS.contains(card.path("border_color").asText())
                && Streams.stream(card.path("frame_effects")).map(JsonNode::asText).noneMatch(SPECIAL_FRAME_EFFECTS::contains)
                && Streams.stream(card.path("promo_types")).map(JsonNode::asText).noneMatch("boosterfun"::equals)
                && !((frame.equals("1993") || frame.equals("1997")) && !releasedAt.isBefore(MODERN_FRAME_START))
                && !(games.size() == 1 && "arena".equals(games.path(0).asText())); // Arena-only, no paper printing
    }
}
