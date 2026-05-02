package io.banditoz.mchelper.database;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.database.transaction.DatabaseTransactionManager;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@InjectTest
public class DatabaseTransactionTests {
    @Inject
    Database database;
    @Inject
    DatabaseTransactionManager tx;

    @Test
    void testDatabaseTransaction() throws Throwable {
        try (Connection rawConnection = database.getRawConnection()) {
            Query.of("CREATE TABLE test (id SERIAL, content text);")
                    .execute(rawConnection);
        }

        tx.runInTx(() -> {
            try (Connection connection = database.getConnection()) {
                System.out.println(connection);
                Query.of("INSERT INTO test (content) VALUES ('hello');")
                        .execute(connection);
                Query.of("INSERT INTO test (content) VALUES ('world');")
                        .execute(connection);

                // check raw connection before commit
                try (Connection rawConnection = database.getRawConnection()) {
                    int numOfRows = Query.of("SELECT COUNT(*) AS count FROM test;")
                            .as((rs, conn) -> {
                                rs.next();
                                return rs.getInt("count");
                            }, rawConnection);
                    assertThat(numOfRows).isEqualTo(0);
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        // should be committed by this point

        try (Connection rawConnection = database.getRawConnection()) {
            int numOfRows = Query.of("SELECT COUNT(*) AS count FROM test;")
                    .as((rs, conn) -> {
                        rs.next();
                        return rs.getInt("count");
                    }, rawConnection);
            assertThat(numOfRows).isEqualTo(2);
        }
    }
}
