package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class EvalCommandTests extends BaseCommandTest {
    @Inject
    EvalCommand ec;

    @Test
    void testEvalCommand() throws Exception {
        setArgs("""
                ```java
                int x = 5;
                return x;
                ```""");
        ec.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("```\n5```");
    }

    @Test
    void testEvalCommandExternalClass() throws Exception {
        setArgs("""
                ```java
                import com.udojava.evalex.Expression;
                
                return new Expression("1+1").eval()
                ```""");
        ec.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("2");
    }


    @Test
    void testEvalCommandNull() throws Exception {
        setArgs("null");
        ec.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("<null output>");
    }
}
