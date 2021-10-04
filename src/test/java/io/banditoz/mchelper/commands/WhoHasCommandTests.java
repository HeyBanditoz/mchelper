package io.banditoz.mchelper.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class WhoHasCommandTests extends BaseCommandTest {
    private final WhoHasCommand whc;

    public WhoHasCommandTests() {
        this.whc = spy(new WhoHasCommand());
    }

    @Test
    public void testWhoHasCommand() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("732863308738330636");
        whc.onCommand(ce);
        assertEquals("Members that have role *Role:*\n<@!404837963697225729>, <@!163094867910590464>", embedCaptor.getValue().getDescription());
    }

    @Test
    public void testWhoHasCommandInvalidRole() {
        when(ce.getCommandArgsString()).thenReturn("0");
        assertThrows(NullPointerException.class, () -> whc.onCommand(ce));
    }
}
