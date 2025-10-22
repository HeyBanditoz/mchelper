package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class MorseCommandsTests extends BaseCommandTest {
    @Inject
    ToMorseCommand tmc;
    @Inject
    FromMorseCommand fmc;

    @Test
    void testToMorseCode() throws Exception {
        setArgs("this is a test");
        tmc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualToNormalizingWhitespace("- .... .. ... / .. ... / .- / - . ... -");
    }

    @Test
    void testFromMorseCodeInvalidInput() throws Exception {
        setArgs("- .... .. ... / .. ... / .- / invalid 123");
        fmc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("THIS IS A ■■");
    }
}
