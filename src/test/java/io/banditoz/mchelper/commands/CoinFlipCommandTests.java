package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

public class CoinFlipCommandTests extends BaseCommandTest {
    private final CoinFlipCommand cfc;

    public CoinFlipCommandTests() {
        this.cfc = spy(new CoinFlipCommand());
        doNothing().when(ce).sendReply(stringCaptor.capture());
    }

    @Test
    public void testCoinFlipCommand() throws Exception {
        cfc.onCommand(ce);
        assertThat(stringCaptor.getValue()).containsAnyOf("Heads!", "Tails!");
    }
}
