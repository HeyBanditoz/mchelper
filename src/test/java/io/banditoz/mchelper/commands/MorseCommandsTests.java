package io.banditoz.mchelper.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MorseCommandsTests extends BaseCommandTest {
    private final ToMorseCommand tmc;
    private final FromMorseCommand fmc;

    public MorseCommandsTests() {
        this.tmc = spy(new ToMorseCommand());
        this.fmc = spy(new FromMorseCommand());
    }

    @Test
    public void testToMorseCode() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("this is a test");
        tmc.onCommand(ce);
        assertEquals("- .... .. ... / .. ... / .- / - . ... - ", stringCaptor.getValue()); // yeah, ignore the space
    }

    @Test
    public void testFromMorseCodeInvalidInput() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("- .... .. ... / .. ... / .- / invalid 123");
        fmc.onCommand(ce);
        assertEquals("THIS IS A ■■", stringCaptor.getValue());
    }
}
