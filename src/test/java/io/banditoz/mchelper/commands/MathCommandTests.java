package io.banditoz.mchelper.commands;

import com.udojava.evalex.Expression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertEquals("16", stringCaptor.getValue());
    }

    @Test
    public void testMathCommandEngineeringString() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("10^512");
        mc.onCommand(ce);
        assertEquals("100E+510", stringCaptor.getValue());
    }


    @Test
    public void testBadInput() {
        when(ce.getCommandArgsString()).thenReturn("hello world"); // -> 16
        assertThrows(Expression.ExpressionException.class, () -> mc.onCommand(ce));
    }
}
