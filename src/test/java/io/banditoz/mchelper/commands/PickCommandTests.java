package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;

public class PickCommandTests extends BaseCommandTest {
    private final PickCommand pc;

    public PickCommandTests() {
        this.pc = spy(new PickCommand());
    }

    @Test
    public void testPickCommand() throws Exception {
        setArgs("this that these those");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this", "that", "these", "those");
    }

    @Test
    public void testPickCommandWithCount() throws Exception {
        setArgs("2 this that");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this, that", "that, this");
    }

    @Test
    public void testPickCommandWithOr() throws Exception {
        setArgs("this or that or these or those");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this", "that", "these", "those");
    }

    @Test
    public void testPickCommandWithInvalidCount() {
        setArgs("8 this that these those");
        assertThatThrownBy(() -> pc.onCommand(ce));
    }
}
