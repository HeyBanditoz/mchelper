package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@InjectTest
class PickCommandTests extends BaseCommandTest {
    @Inject
    PickCommand pc;

    @Test
    void testPickCommand() throws Exception {
        setArgs("this that these those");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this", "that", "these", "those");
    }

    @Test
    void testPickCommandWithCount() throws Exception {
        setArgs("2 this that");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this, that", "that, this");
    }

    @Test
    void testPickCommandWithOr() throws Exception {
        setArgs("this or that or these or those");
        pc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("this", "that", "these", "those");
    }

    @Test
    void testPickCommandWithInvalidCount() {
        setArgs("8 this that these those");
        assertThatThrownBy(() -> pc.onCommand(ce));
    }
}
