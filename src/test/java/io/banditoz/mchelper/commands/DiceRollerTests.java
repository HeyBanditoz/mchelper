package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
public class DiceRollerTests extends BaseCommandTest {
    @Inject
    DiceRollerCommand drc;

    @Test
    public void testDiceRollerCommand() throws Exception {
        setArgs("1d20");
        drc.onCommand(ce);
        assertThat(stringCaptor.getValue()).matches("\\d+ = \\d+\\[\\d+]");
    }
}
