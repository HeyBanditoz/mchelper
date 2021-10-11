package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class NowCommandTests extends BaseCommandTest {
    private final NowCommand nc = new NowCommand();

    @Test
    public void testNowCommand() throws Exception {
        nc.onCommand(ce);
        String result = stringCaptor.getValue();
        assertThat(result).startsWith("<t:").endsWith(">");
        long unixTime = Long.parseLong(result.substring(3, 13)) * 1000;
        assertThat(unixTime).isCloseTo(System.currentTimeMillis(), within(10000L));
    }
}
