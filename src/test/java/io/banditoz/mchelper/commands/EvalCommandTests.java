package io.banditoz.mchelper.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.testng.annotations.Test;

public class EvalCommandTests extends BaseCommandTest {
    private final EvalCommand ec;

    public EvalCommandTests() {
        this.ec = spy(new EvalCommand(null));
        when(ce.getEvent().isFromType(any())).thenReturn(true);
    }

    @Test
    public void testEvalCommand() throws Exception {
        setArgs("""
                ```java
                int x = 5;
                return x;
                ```""");
        ec.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("```\n5```");
    }

    @Test
    public void testEvalCommandExternalClass() throws Exception {
        setArgs("""
                ```java
                import com.udojava.evalex.Expression;
                
                return new Expression("1+1").eval()
                ```""");
        ec.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("2");
    }


    @Test
    public void testEvalCommandNull() throws Exception {
        setArgs("null");
        ec.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("<null output>");
    }
}
