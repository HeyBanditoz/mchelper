package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertTrue(TestUtils.containsString(new String[]{"Heads!", "Tails!"}, stringCaptor.getValue()));
    }
}
