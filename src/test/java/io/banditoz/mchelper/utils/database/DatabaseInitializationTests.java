package io.banditoz.mchelper.utils.database;

import io.opentelemetry.api.OpenTelemetry;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

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
