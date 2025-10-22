package io.banditoz.mchelper;

import io.avaje.inject.test.InjectTest;
import io.avaje.inject.test.TestScope;
import io.banditoz.mchelper.database.Database;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base test class <i>most</i> tests should inherit from.<br>
 * For simplicity, the test suite can't be run without the database.
 */
@InjectTest
@TestScope
public class BaseTest {
    @Inject
    Database database;

    /** Truncates some tables. If the table doesn't exist, this will probably fail. */
    public void truncate(String... tables) {
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            for (String table : tables) {
                Query.of("TRUNCATE TABLE " + table + " CASCADE").execute(c);
            }
            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** Resets some SQL sequences, for autoincrementing IDs. */
    public void resetSequence(String... sequenceNames) {
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            for (String seq : sequenceNames) {
                Query.of("ALTER SEQUENCE " + seq + " RESTART WITH 1").execute(c);
            }
            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
