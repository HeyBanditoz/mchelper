package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(stringCaptor.getValue()).isEqualToNormalizingWhitespace("- .... .. ... / .. ... / .- / - . ... -");
    }

    @Test
    public void testFromMorseCodeInvalidInput() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("- .... .. ... / .. ... / .- / invalid 123");
        fmc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("THIS IS A ■■");
    }
}
