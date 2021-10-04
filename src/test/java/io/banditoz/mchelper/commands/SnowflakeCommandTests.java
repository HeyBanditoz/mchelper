package io.banditoz.mchelper.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SnowflakeCommandTests extends BaseCommandTest {
    private final SnowflakeCommand sc;

    public SnowflakeCommandTests() {
        this.sc = spy(new SnowflakeCommand());
    }

    @Test
    public void testSnowflakeCommand() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("572466277638209547 125227483518861312");
        sc.onCommand(ce);
        assertEquals("572466277638209547 -> <t:1556556996:F>\n125227483518861312 -> <t:1449926958:F>\n", stringCaptor.getValue());
    }

    @Test
    public void testSnowflakeCommandInvalidInput() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("foobarman");
        sc.onCommand(ce);
        assertEquals("No snowflake IDs found or IDs are invalid.", stringCaptor.getValue());
    }
}
