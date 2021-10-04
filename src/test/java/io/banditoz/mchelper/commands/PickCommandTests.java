package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class PickCommandTests extends BaseCommandTest {
    private final PickCommand pc;

    public PickCommandTests() {
        this.pc = spy(new PickCommand());
    }

    @Test
    public void testPickCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!pick", "this"});
        when(ce.getCommandArgsString()).thenReturn("this that these those");
        pc.onCommand(ce);
        assertTrue(TestUtils.containsString(new String[]{"this", "that", "these", "those"}, stringCaptor.getValue()));
    }

    @Test
    public void testPickCommandWithCount() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!pick", "2"});
        when(ce.getCommandArgsString()).thenReturn("this that");
        pc.onCommand(ce);
        String s = stringCaptor.getValue();
        assertTrue(s.equals("this, that") || s.equals("that, this"));
    }

    @Test
    public void testPickCommandWithOr() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!pick", "this"});
        when(ce.getCommandArgsString()).thenReturn("this or that or these or those");
        pc.onCommand(ce);
        assertTrue(TestUtils.containsString(new String[]{"this", "that", "these", "those"}, stringCaptor.getValue()));
    }

    @Test
    public void testPickCommandWithInvalidCount() {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!pick", "8"});
        when(ce.getCommandArgsString()).thenReturn("this that these those");
        assertThrows(Exception.class, () -> pc.onCommand(ce));
    }
}
