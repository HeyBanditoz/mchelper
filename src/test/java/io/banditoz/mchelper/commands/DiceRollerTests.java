package io.banditoz.mchelper.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DiceRollerTests extends BaseCommandTest {
    private final DiceRollerCommand drc;

    public DiceRollerTests() {
        this.drc = spy(new DiceRollerCommand());
    }

    @Test
    public void testDiceRollerCommand() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("1d20");
        drc.onCommand(ce);
        Assertions.assertTrue(stringCaptor.getValue().matches("\\d+ = \\d+\\[\\d+]"));
    }
}
