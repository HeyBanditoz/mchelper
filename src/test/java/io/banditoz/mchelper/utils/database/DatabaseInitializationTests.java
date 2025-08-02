package io.banditoz.mchelper.utils.database;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.banditoz.mchelper.database.Database;
import io.opentelemetry.api.OpenTelemetry;
import org.testng.SkipException;
import org.testng.annotations.Test;

@Test(groups = {"DatabaseInitializationTests"})
public class DatabaseInitializationTests {
    @Test
    public void testTableCreation() {
        if (!Database.isConfigured()) {
            throw new SkipException("The database is not configured.");
        }
        assertThatCode(() -> {
            Database d = new Database(OpenTelemetry.noop());
            d.migrate(false);
        }).doesNotThrowAnyException();
    }
}
