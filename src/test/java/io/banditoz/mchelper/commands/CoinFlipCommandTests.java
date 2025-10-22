package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class CoinFlipCommandTests extends BaseCommandTest {
    @Inject
    CoinFlipCommand cfc;

    @Test
    public void testCoinFlipCommand() throws Exception {
        cfc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("Heads!", "Tails!");
    }
}
