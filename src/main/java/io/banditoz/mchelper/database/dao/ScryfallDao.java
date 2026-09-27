package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.Optional;

import io.banditoz.mchelper.database.ScryfallCardRow;
import io.banditoz.mchelper.mtg.ScryfallCard;

public interface ScryfallDao {
    /**
     * Deletes all cards then re-imports the passed in ones.
     *
     * @param rows The cards to import. Consumed lazily.
     * @return The number of rows imported.
     */
    int replaceAll(Iterator<ScryfallCardRow> rows) throws SQLException;

    /**
     * @param search The name as the user typed it.
     * @return The best matching card, as its most regular recent English printing, or empty if nothing is close.
     */
    Optional<ScryfallCard> findByFuzzyName(String search) throws SQLException;

    /** @return When the most recent import happened, or empty if nothing has been imported yet. */
    Optional<OffsetDateTime> lastImportedAt() throws SQLException;
}
