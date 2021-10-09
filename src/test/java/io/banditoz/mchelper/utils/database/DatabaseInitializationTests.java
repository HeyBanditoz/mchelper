package io.banditoz.mchelper.utils.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DatabaseInitializationTests {
    @Test
    public void testTableCreation() {
        Assertions.assertDoesNotThrow(Database::new);
    }
}
