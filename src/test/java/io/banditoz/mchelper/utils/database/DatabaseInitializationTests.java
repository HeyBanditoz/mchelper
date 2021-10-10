package io.banditoz.mchelper.utils.database;

import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

public class DatabaseInitializationTests {
    @Test
    public void testTableCreation() {
        if (!Database.isConfigured()) {
            throw new SkipException("The database is not configured.");
        }
        assertThatCode(Database::new).doesNotThrowAnyException();
    }
}
