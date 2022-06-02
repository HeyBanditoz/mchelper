package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class DiceRollerTests extends BaseCommandTest {
    private final DiceRollerCommand drc;

    public DiceRollerTests() {
        this.drc = spy(new DiceRollerCommand());
    }

    @Test
    public void testDiceRollerCommand() throws Exception {
        setArgs("1d20");
        drc.onCommand(ce);
        assertThat(stringCaptor.getValue()).matches("\\d+ = \\d+\\[\\d+]");
    }
}
