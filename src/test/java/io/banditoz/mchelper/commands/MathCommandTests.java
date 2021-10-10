package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class MathCommandTests extends BaseCommandTest {
    private final MathCommand mc;

    public MathCommandTests() {
        this.mc = spy(new MathCommand());
        doNothing().when(ce).sendReply(stringCaptor.capture());
    }

    @Test
    public void testMathCommandPlainString() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("1+3*5");
        mc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("16");
    }

    @Test
    public void testMathCommandEngineeringString() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("10^512");
        mc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("100E+510");
    }


    @Test
    public void testBadInput() {
        when(ce.getCommandArgsString()).thenReturn("hello world"); // -> 16
        assertThatThrownBy(() -> mc.onCommand(ce));
    }
}
