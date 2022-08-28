package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class SnowflakeCommandTests extends BaseCommandTest {
    private final SnowflakeCommand sc;

    public SnowflakeCommandTests() {
        this.sc = spy(new SnowflakeCommand());
    }

    @Test
    public void testSnowflakeCommand() throws Exception {
        setArgs("572466277638209547 125227483518861312");
        sc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("572466277638209547 -> <t:1556556996:F>\n125227483518861312 -> <t:1449926958:F>\n");
    }

    @Test
    public void testSnowflakeCommandLongDigits() throws Exception {
        setArgs("1013556610024624229");
        sc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("1013556610024624229 -> <t:1661721126:F>\n");
    }

    @Test
    public void testSnowflakeCommandInvalidInput() throws Exception {
        setArgs("foobarman");
        sc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("No snowflake IDs found or IDs are invalid.");
    }
}
