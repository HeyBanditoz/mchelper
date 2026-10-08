package io.banditoz.mchelper.database.dao;

import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;

import static io.jenetics.facilejdbc.Dctor.field;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.database.ScryfallCardRow;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.mtg.ScryfallCard;
import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresDatabase
public class ScryfallDaoImpl extends Dao implements ScryfallDao {
    private static final int INSERT_BATCH_SIZE = 100;
    private final ObjectMapper objectMapper;

    @Inject
    public ScryfallDaoImpl(Database database, ObjectMapper objectMapper) {
        super(database);
        this.objectMapper = objectMapper;
    }

    @Override
    public int replaceAll(Iterator<ScryfallCardRow> rows) throws SQLException {
        OffsetDateTime importedAt = OffsetDateTime.now();
        Dctor<ScryfallCardRow> dctor = Dctor.of(
                field("id", ScryfallCardRow::id),
                field("oid", ScryfallCardRow::oracleId),
                field("n", ScryfallCardRow::name),
                field("sn", ScryfallCardRow::searchName),
                field("l", ScryfallCardRow::lang),
                field("s", ScryfallCardRow::setCode),
                field("cn", ScryfallCardRow::collectorNumber),
                field("r", ScryfallCardRow::releasedAt),
                field("lay", ScryfallCardRow::layout),
                field("np", ScryfallCardRow::normalPrint),
                field("er", ScryfallCardRow::edhrecRank),
                field("d", ScryfallCardRow::data),
                Dctor.fieldValue("ia", importedAt)
        );
        Query insert = Query.of("""
                INSERT INTO mtg_cards
                (id, oracle_id, name, search_name, lang, set_code, collector_number, released_at, layout,
                 normal_print, edhrec_rank, data, imported_at)
                VALUES (:id, :oid, :n, :sn, :l, :s, :cn, :r, :lay, :np, :er, CAST(:d AS jsonb), :ia)""");
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(insert.sql())) {
                // MVCC will allow the rare read to still happen while updating in the txn
                Query.of("DELETE FROM mtg_cards").executeUpdate(c);
                int count = 0;
                while (rows.hasNext()) {
                    dctor.unapply(rows.next(), c).set(insert.paramNames(), ps);
                    ps.addBatch();
                    if (++count % INSERT_BATCH_SIZE == 0) {
                        executeBatch(ps);
                    }
                }
                if (count % INSERT_BATCH_SIZE != 0) {
                    executeBatch(ps);
                }
                c.commit();
                return count;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private static void executeBatch(PreparedStatement ps) throws SQLException {
        ps.executeBatch();
        ps.clearBatch();
    }

    @Override
    public Optional<ScryfallCard> findByFuzzyName(String search) throws SQLException {
        Optional<String> json;
        try (Connection c = database.getConnection()) {
            json = Query.of("""
                    WITH candidates AS (
                        SELECT oracle_id, edhrec_rank,
                               search_name = :q AS exact,
                               search_name LIKE :q || ' //%' OR search_name LIKE '%// ' || :q AS exact_face,
                               search_name LIKE :q || ' %' OR search_name LIKE '%// ' || :q || ' %' AS word_prefix,
                               word_similarity(:q, search_name) AS word_sim,
                               similarity(:q, search_name) AS sim
                        FROM mtg_cards
                        WHERE :q <% search_name
                    ), card AS (
                        SELECT oracle_id
                        FROM candidates
                        ORDER BY exact DESC,
                                 exact_face DESC,
                                 exact OR exact_face OR word_prefix DESC,
                                 CASE WHEN exact OR exact_face OR word_prefix THEN edhrec_rank END ASC NULLS LAST,
                                 word_sim DESC,
                                 sim DESC,
                                 oracle_id
                        LIMIT 1
                    )
                    SELECT data
                    FROM mtg_cards
                    WHERE oracle_id = (SELECT oracle_id FROM card)
                    ORDER BY lang = 'en' DESC,
                             normal_print DESC,
                             released_at DESC,
                             substring(collector_number FROM '^[0-9]+')::int ASC NULLS LAST,
                             collector_number,
                             id
                    LIMIT 1""")
                    .on(Param.value("q", search.toLowerCase(Locale.ROOT)))
                    .as((rs, conn) -> rs.next() ? Optional.of(rs.getString(1)) : Optional.empty(), c);
        }
        try {
            return json.isPresent() ? Optional.of(objectMapper.readValue(json.get(), ScryfallCard.class)) : Optional.empty();
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<OffsetDateTime> lastImportedAt() throws SQLException {
        try (Connection c = database.getConnection()) {
            return Query.of("SELECT MAX(imported_at) FROM mtg_cards")
                    .as((rs, conn) -> {
                        rs.next();
                        return Optional.ofNullable(rs.getObject(1, OffsetDateTime.class));
                    }, c);
        }
    }
}
