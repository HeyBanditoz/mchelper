package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@InjectTest
class MathCommandTests extends BaseCommandTest {
    @Inject
    MathCommand mc;

    @Test
    void testMathCommandPlainString() throws Exception {
        setArgs("1+3*5");
        mc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("16");
    }

    @Test
    void testMathCommandEngineeringString() throws Exception {
        setArgs("10^512");
        mc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("100E+510");
    }


    @Test
    void testBadInput() {
        setArgs("hello world"); // -> 16
        assertThatThrownBy(() -> mc.onCommand(ce));
    }
}
