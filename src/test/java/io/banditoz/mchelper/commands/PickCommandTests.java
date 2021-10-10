package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        assertThat(stringCaptor.getValue()).containsAnyOf("this", "that", "these", "those");
    }

    @Test
    public void testPickCommandWithCount() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!pick", "2"});
        when(ce.getCommandArgsString()).thenReturn("this that");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this, that", "that, this");
    }

    @Test
    public void testPickCommandWithOr() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!pick", "this"});
        when(ce.getCommandArgsString()).thenReturn("this or that or these or those");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this", "that", "these", "those");
    }

    @Test
    public void testPickCommandWithInvalidCount() {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!pick", "8"});
        when(ce.getCommandArgsString()).thenReturn("this that these those");
        assertThatThrownBy(() -> pc.onCommand(ce));
    }
}
