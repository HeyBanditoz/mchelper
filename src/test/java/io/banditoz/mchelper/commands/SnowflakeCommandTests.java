package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class SnowflakeCommandTests extends BaseCommandTest {
    @Inject
    SnowflakeCommand sc;

    @Test
    void testSnowflakeCommand() throws Exception {
        setArgs("572466277638209547 125227483518861312");
        sc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("572466277638209547 -> <t:1556556996:F>\n125227483518861312 -> <t:1449926958:F>\n");
    }

    @Test
    void testSnowflakeCommandLongDigits() throws Exception {
        setArgs("1013556610024624229");
        sc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("1013556610024624229 -> <t:1661721126:F>\n");
    }

    @Test
    void testSnowflakeCommandInvalidInput() throws Exception {
        setArgs("foobarman");
        sc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("No snowflake IDs found or IDs are invalid.");
    }
}
